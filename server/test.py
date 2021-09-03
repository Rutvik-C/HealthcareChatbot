# from bs4 import BeautifulSoup
# import requests
# import re
# from googlesearch import search
#
#
# def diseaseDetail(term):
#     diseases = [term]
#     ret = [term]
#     for dis in diseases:
#         # search "disease wikipedia" on google
#         query = dis + ' wikipedia'
#
#         for sr in search(query, tld="co.in", num=10, stop=10, pause=2):
#             # open wikipedia link
#             match = re.search(r'wikipedia', sr)
#             filled = 0
#             if match:
#                 wiki = requests.get(sr, verify=False)
#                 soup = BeautifulSoup(wiki.content, 'html.parser')
#                 # Fetch HTML code for 'infobox'
#                 info_table = soup.find("table", {"class": "infobox"})
#                 if info_table is not None:
#                     # Preprocess contents of infobox
#                     for row in info_table.find_all("tr"):
#                         data = row.find("th", {"scope": "row"})
#                         if data is not None:
#                             symptom = str(row.find("td"))
#                             symptom = symptom.replace('.', '')
#                             symptom = symptom.replace(';', ',')
#                             symptom = symptom.replace('<b>', '<b> \n')
#                             symptom = re.sub(r'<a.*?>', '', symptom)  # Remove hyperlink
#                             symptom = re.sub(r'</a>', '', symptom)  # Remove hyperlink
#                             symptom = re.sub(r'<[^<]+?>', ' ', symptom)  # All the tags
#                             symptom = re.sub(r'\[.*\]', '', symptom)  # Remove citation text
#                             symptom = symptom.replace("&gt", ">")
#
#                             if data.get_text() not in ["Pronunciation"]:
#                                 ret.append(data.get_text() + ": " + symptom)
#                             filled = 1
#                 if filled:
#                     break
#     return ret
#
#
# info = diseaseDetail('')
# if len(info) == 0:
#     print("Unable to fetch info")
# else:
#     for i in info:
#         print(i)

from spellchecker import SpellChecker

spell = SpellChecker()

# find those words that may be misspelled
misspelled = spell.unknown(['malaria'])
print(misspelled)
for word in misspelled:
    print(spell.correction(word), type(spell.correction(word)))

print("done")
