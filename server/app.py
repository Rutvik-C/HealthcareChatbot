import json
import pickle
import random
from collections import Counter
from itertools import combinations
import nltk
import numpy as np
import pandas as pd
import requests
from bs4 import BeautifulSoup
from flask import Flask, request, jsonify
from nltk.corpus import wordnet, stopwords
from nltk.stem import WordNetLemmatizer
from nltk.tokenize import RegexpTokenizer
from nltk.tokenize import word_tokenize
from tensorflow import keras
from googlesearch import search
import re
import string
from spellchecker import SpellChecker
import warnings

warnings.simplefilter("ignore")

nltk.download('stopwords')
nltk.download('wordnet')
lemmatizer = WordNetLemmatizer()
stop_words = stopwords.words('english')
splitter = RegexpTokenizer(r'\w+')
spell = SpellChecker()

app = Flask(__name__)

with open("./models/amazon_tfidf_vector.pkl", "rb") as f:
    vectorizer = pickle.load(f)
with open("./models/linear_reg.pkl", "rb") as f:
    linear_reg = pickle.load(f)
with open("./data/responses.json", "r") as f:
    responses = json.load(f)
intent_classifier = keras.models.load_model("./models/amazon_cb.h5")

df_comb = pd.read_csv("./data/dis_sym_dataset_comb.csv")
df_norm = pd.read_csv("./data/dis_sym_dataset_norm.csv")
X = df_norm.iloc[:, 1:]
dataset_symptoms = list(X.columns)
dataset_diseases = list(set(df_comb["label_dis"].values))

user_intent = {"previous": [], "symptoms": [], "disease": None}


def diseaseDetail(term):
    diseases = [term]
    ret = ["Some facts and information about " + term]
    for dis in diseases:
        # search "disease wikipedia" on google
        query = dis + ' wikipedia'

        for sr in search(query, tld="co.in", num=10, stop=10, pause=2):
            # open wikipedia link
            match = re.search(r'wikipedia', sr)
            filled = 0
            if match:
                wiki = requests.get(sr, verify=False)
                soup = BeautifulSoup(wiki.content, 'html.parser')
                # Fetch HTML code for 'infobox'
                info_table = soup.find("table", {"class": "infobox"})
                if info_table is not None:
                    # Preprocess contents of infobox
                    for row in info_table.find_all("tr"):
                        data = row.find("th", {"scope": "row"})
                        if data is not None:
                            symptom = str(row.find("td"))
                            symptom = symptom.replace('.', '')
                            symptom = symptom.replace(';', ',')
                            symptom = symptom.replace('<b>', '<b> \n')
                            symptom = re.sub(r'<a.*?>', '', symptom)  # Remove hyperlink
                            symptom = re.sub(r'</a>', '', symptom)  # Remove hyperlink
                            symptom = re.sub(r'<[^<]+?>', ' ', symptom)  # All the tags
                            symptom = re.sub(r'\[.*\]', '', symptom)  # Remove citation text
                            symptom = symptom.replace("&gt", ">")

                            if data.get_text() not in ["Pronunciation"]:
                                ret.append(data.get_text() + ": " + symptom)
                            filled = 1
                if filled:
                    break
    return ret


def process_text(message):
    words = nltk.word_tokenize(message)
    words = [lemmatizer.lemmatize(x.lower()) for x in words]
    lematized = " ".join(words)

    vec = vectorizer.transform([lematized])
    return vec.toarray()


def synonyms(term):
    list_synonyms = []
    response = requests.get('https://www.thesaurus.com/browse/{}'.format(term))
    soup = BeautifulSoup(response.content, "html.parser")
    try:
        container = soup.find('section', {'class': 'MainContentContainer'})
        row = container.find('div', {'class': 'css-191l5o0-ClassicContentCard'})
        row = row.find_all('li')
        for x in row:
            list_synonyms.append(x.get_text())

    except:
        pass
    for syn in wordnet.synsets(term):
        list_synonyms += syn.lemma_names()

    return set(list_synonyms)


def get_co_occurring_symptoms(symptom_list):
    dis_list = set()
    counter_list = []

    for sym in symptom_list:
        dis_list.update(set(df_norm[df_norm[sym] == 1]['label_dis']))

    for dis in dis_list:
        row = df_norm.loc[df_norm['label_dis'] == dis].values.tolist()
        row[0].pop(0)
        for idx, val in enumerate(row[0]):
            if val != 0 and dataset_symptoms[idx] not in symptom_list:
                counter_list.append(dataset_symptoms[idx])

    dict_sym = dict(Counter(counter_list))
    dict_sym_tup = sorted(dict_sym.items(), key=lambda x: x[1], reverse=True)

    return [x[0] for x in dict_sym_tup[: 5]]


def preprocess_symptom_text(text):
    text = text.lower().split(",")

    processed_user_symptoms = []
    for sym in text:
        sym = sym.strip()
        sym = sym.replace('-', ' ')
        sym = sym.replace("'", '')
        sym = ' '.join([lemmatizer.lemmatize(word) for word in splitter.tokenize(sym)])
        processed_user_symptoms.append(sym)

    print(f"processed user sym: {processed_user_symptoms}")

    user_symptoms = []
    for user_sym in processed_user_symptoms:
        user_sym = user_sym.split()
        str_sym = set()
        for comb in range(1, len(user_sym) + 1):
            for subset in combinations(user_sym, comb):
                subset = ' '.join(subset)
                subset = synonyms(subset)
                str_sym.update(subset)

        str_sym.add(' '.join(user_sym))
        user_symptoms.append(' '.join(str_sym).replace('_', ' '))

    print(f"user symptoms: {user_symptoms}")

    found_symptoms = set()
    for idx, data_sym in enumerate(dataset_symptoms):
        data_sym_split = data_sym.split()
        for user_sym in user_symptoms:
            count = 0
            for symp in data_sym_split:
                if symp in user_sym.split():
                    count += 1
            if count / len(data_sym_split) > 0.5:
                found_symptoms.add(data_sym)

    print(f"Found symptoms: {found_symptoms}")
    return list(found_symptoms)


def predict_disease(symptoms):
    sample_x = [0 for _ in range(0, len(dataset_symptoms))]

    for val in symptoms:
        sample_x[dataset_symptoms.index(val)] = 1

    pred = linear_reg.predict([sample_x])
    return pred[0].title()


def extract_and_fetch_disease_info(text):
    text = text.lower()

    # deleting punctuations
    rm_list = string.punctuation
    table_ = str.maketrans("", "", rm_list)
    text = text.translate(table_)

    # tokenizing
    text_tokens = word_tokenize(text)
    text = [w for w in text_tokens if w not in stopwords.words()]

    misspelled = spell.unknown(text)
    for w in misspelled:
        text[text.index(w)] = spell.correction(w)

    diseases = [d.lower() for d in dataset_diseases]

    target = None
    for disease in diseases:
        for word in text:
            if word == disease:
                target = disease
                break

    if target is None:
        target = user_intent["disease"]

    if target is None:
        return "I wasn't able to get the disease name.__n__You mind typing it again?"

    info = diseaseDetail(target)
    if len(info) == 0:
        return "I was unable to fetch info"

    else:
        s = ""
        for i, inf in enumerate(info):
            s += inf
            if i != len(info) - 1:
                s += "__n__"
        return s


@app.route("/chat", methods=['POST'])
def reply():
    global user_intent

    try:
        text = request.form["text"]
        user = request.form["user"]
        list_type = request.form["list_type"]
        list_data = [x.strip() for x in request.form["list"][1: -1].split(",") if x != ""]

        print(f"[+] text={text}, user={user}, list_type={list_type}, list_data={list_data}")

        data = None

        # Checking previous intent dependency
        if len(user_intent["previous"]) != 0 and user_intent["previous"][-1] == "__query__":
            print(f"[+] extracting symptoms from user text")
            symptoms = preprocess_symptom_text(text)

            if len(symptoms) == 0:
                data = {
                    "message": "Umm... None of the symptoms matched with the symptoms you've given.__n__Try in a different way",
                    "type": "simple", "list": None, "list_content": None}

            else:
                user_intent["previous"].append("__symptoms__")
                data = {"message": "Select from the below symptoms", "type": "list", "list": symptoms, "list_content": "symptoms"}

        # Fetching list of symptoms
        elif list_type == "symptom_list" and len(user_intent["previous"]) != 0 and user_intent["previous"][-1] == "__symptoms__":
            print(f"[+] reading user selected symptoms")

            if len(list_data) == 0:
                user_intent["previous"].append("__query__")
                data = {"message": "Well try telling the symptoms in bit different way__n__Again in comma separated form :)", "type": "simple", "list": None, "list_content": None}

            else:
                user_intent["symptoms"] = list_data

                co_occurring = get_co_occurring_symptoms(list_data)

                user_intent["previous"].append("__co_occur__")
                data = {"message": "Okay__n__Do you have any of below symptom as well?", "type": "list", "list": co_occurring, "list_content": "additional"}

        # Check if user selects any co occurring
        elif list_type == "add_symptom_list" and len(user_intent["previous"]) != 0 and user_intent["previous"][-1] == "__co_occur__":
            print(f"[+] reading user selected co-occurring symptoms and predicting disease")

            user_intent["symptoms"].extend(list_data)

            # Predict
            disease = predict_disease(user_intent["symptoms"])

            syms = ""
            for i, sym in enumerate(user_intent["symptoms"]):
                syms += sym
                if i == len(user_intent["symptoms"]) - 2:
                    syms += ", and "
                elif i != len(user_intent["symptoms"]) - 1:
                    syms += ", "

            user_intent["previous"].append("__pred__")

            user_intent["disease"] = disease

            message = responses["disease_predicted"][random.randint(0, len(responses["disease_predicted"]) - 1)]
            message = message.replace("__p__", disease)
            message += "__n__Based on your symptoms " + syms
            message += "__n__Should I search for nearby hospitals?"

            data = {"message": message, "type": "simple", "list": None, "list_content": None}

        # Predict intent
        else:
            print(f"[+] intent classification")

            vector = process_text(text)
            res = intent_classifier.predict(vector)
            idx = np.argmax(res)

            if res[0][idx] < 0.8:  # Not sure
                print(f"--- low confidence")

                message = responses["below_threshold"][random.randint(0, len(responses["below_threshold"]) - 1)]
                data = {"message": message, "type": "simple", "list": None, "list_content": None}

            else:
                if idx == 0:  # affirmative
                    print(f"--- affirmative")

                    if len(user_intent["previous"]) != 0 and user_intent["previous"][-1] == "__pred__":
                        message = responses["show_map"][random.randint(0, len(responses["show_map"]) - 1)]
                        data = {"message": message, "type": "map", "list": None, "list_content": None}

                elif idx == 1:  # greeting
                    print(f"--- greeting")

                    user_intent["previous"].append("__greeting__")
                    message = responses["greeting"][random.randint(0, len(responses["greeting"]) - 1)]
                    message = message.replace("__u__", user.split("@")[0])

                    data = {"message": message, "type": "simple", "list": None, "list_content": None}

                elif idx == 2:  # information
                    print(f"--- information")

                    user_intent["previous"].append("__info__")
                    message = extract_and_fetch_disease_info(text)
                    data = {"message": message, "type": "simple", "list": None, "list_content": None}

                elif idx == 3:  # nearby
                    message = responses["show_map"][random.randint(0, len(responses["show_map"]) - 1)]
                    data = {"message": message, "type": "map", "list": None, "list_content": None}

                elif idx == 4:  # negative
                    print(f"--- negative")

                elif idx == 5:  # precaution
                    print(f"--- precaution")

                    user_intent["previous"].append("__precaution__")
                    message = extract_and_fetch_disease_info(text)
                    data = {"message": message, "type": "simple", "list": None, "list_content": None}

                elif idx == 6:  # query
                    print(f"--- query")

                    user_intent["previous"].append("__query__")
                    message = responses["tell_symptoms"][random.randint(0, len(responses["tell_symptoms"]) - 1)]
                    message += "__n__Type the symptoms comma separated..."

                    data = {"message": message, "type": "simple", "list": None, "list_content": None}

        resp = jsonify(data)
        resp.status_code = 201
        return resp

    except Exception as e:
        resp = jsonify({"[-] error": e})
        resp.status_code = 400
        return resp


if __name__ == "__main__":
    app.run(host="0.0.0.0", port="5000", threaded=False)
