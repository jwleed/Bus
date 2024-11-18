import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

# Use a service account.
cred = credentials.Certificate('./buss-8a962-firebase-adminsdk-6bwrz-c066282e44.json')

app = firebase_admin.initialize_app(cred)

db = firestore.client()

doc_ref = db.collection("users").document("alovelace")
doc_ref.set({"first": {"a": 5, "b": 766}, "last": "Lovelace", "born": 9999})