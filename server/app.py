from flask import Flask, request, jsonify
import tensorflow as tf
from tensorflow import keras
import nltk
from nltk.stem import WordNetLemmatizer
import sklearn
import pickle
import numpy as np
import json
import random


nltk.download('wordnet')
lemmatizer = WordNetLemmatizer()

app = Flask(__name__)

vectorizer = pickle.load(open("./models/vectorizer.pickel", "rb"))
intent_classifier = keras.models.load_model("./models/amazon_cb.h5")
with open("./data/responses.json") as f:
    responses = json.load(f)

user_intent = {}


def process_text(message):
    words = nltk.word_tokenize(message)
    words = [lemmatizer.lemmatize(x.lower()) for x in words]
    lematized = " ".join(words)

    vec = vectorizer.transform([lematized])
    return vec.toarray()


@app.route("/chat", methods=['POST'])
def reply():
    global user_intent

    try:
        text = request.form["text"]
        user = request.form["user"]
        list_type = request.form["list_type"]
        list_ = [x.strip() for x in request.form["list"][1: -1].split(",")]

        print(f"text={text}:{type(text)}, user={user}:{type(user)}, list_type={list_type}:{type(list_type)}, list={list_}:{type(list_)}")

        data = None

        if list_type != "null":
            if list_type == "symptoms":
                user_intent["symptoms"] = list_.copy()

                # co_occurring_symptoms = get_similar_symptoms(list_)
                co_occurring_symptoms = ["coughing", "headache", "stomachache"]

                message = responses["similar_symptoms"][random.randint(0, len(responses["similar_symptoms"]) - 1)]
                data = {"message": message, "type": "list", "list": co_occurring_symptoms}

            elif list_type == "additional_symptoms":
                # make prediction
                if list_[0] != "null":
                    user_intent["symptoms"].extend(list_)

                print(f"symptoms={user_intent['symptoms']}")
                # predicted_disease = predict_disease(user_intent["symptoms"])
                predicted_disease = "Common cold"
                user_intent["disease"] = predicted_disease

                message = responses["disease_predicted"][random.randint(0, len(responses["disease_predicted"]) - 1)]
                message = message.replace("__p__", predicted_disease)
                data = {"message": message, "type": "simple", "list": None}

        else:
            vector = process_text(text)
            res = intent_classifier.predict(vector)
            idx = np.argmax(res)

            print(f"idx={idx} and result={res}, {res[0][idx]}")

            if res[0][idx] < 0.8:  # Not sure
                message = responses["below_threshold"][random.randint(0, len(responses["below_threshold"]) - 1)]
                print(f"Low conf {message}")
                data = {"message": message, "type": "simple", "list": None}

            else:
                if idx == 0:  # affirmative
                    pass

                elif idx == 1:  # greeting
                    message = responses["greeting"][random.randint(0, len(responses["greeting"]) - 1)]
                    message = message.replace("__u__", user.split("@")[0])

                    print(f"greeting {message}")
                    data = {"message": message, "type": "simple", "list": None}

                elif idx == 2:  # information
                    pass

                elif idx == 3:  # negative
                    pass

                elif idx == 4:  # precaution
                    pass

                elif idx == 5:  # query
                    message = responses["tell_symptoms"][random.randint(0, len(responses["tell_symptoms"]) - 1)]

                    data = {"message": message, "type": "pick_symptom", "list": None}

        resp = jsonify(data)
        resp.status_code = 201
        return resp

    except Exception as e:
        resp = jsonify({"Error": e})
        resp.status_code = 400
        return resp


if __name__ == "__main__":
    app.run(host="0.0.0.0", port="5000", threaded=False)
