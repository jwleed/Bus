from selenium import webdriver
from selenium.webdriver.common.by import By
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import time

# Use a service account.
cred = credentials.Certificate('./buss-8a962-firebase-adminsdk-6bwrz-c066282e44.json')
app = firebase_admin.initialize_app(cred)
db = firestore.client()

driver_path = "C:\chromedriver_win32\chromedriver.exe"  # ChromeDriver가 설치된 위치를 입력하세요.
url = 'http://www.hoseo.ac.kr/Home/Contents.mbz?action=MAPP_2302082206'


# 1. 브라우저 열기
driver = webdriver.Chrome()
driver.get(url)

# 2. 페이지가 완전히 로드될 때까지 잠시 대기
time.sleep(3)

# 3. XPath를 사용하여 요소 찾기 (예: 특정 텍스트가 있는 div 요소)
try:
    # 예: XPath가 '//*[@id="some-id"]/div[2]/span'인 요소를 찾음
    element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]/div[5]/table')
    
    # 요소의 텍스트 출력
    value = element.text.split('도착')[4].split('\n')
    print(value)
    value.pop(0)
    doc_ref = db.collection("main").document("time")
    for x in range(len(value)):
        result = value[x].split(' ')
        result.pop(0)
        doc_ref.set({"아-천": {str(x + 1): result[:len(result) // 2]}, "천-아": {str(x + 1): result[len(result) // 2:]}}, merge=True)

except Exception as e:
    print(f"Element not found: {e}")

# 4. 브라우저 닫기
driver.quit()

