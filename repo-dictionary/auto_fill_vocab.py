"""
AUTO-FILL VOCABULARY with Google Gemini (2.0 Flash)
===================================================
Dung 3 API Key xoay vong de tranh Rate Limit ban Free.
Model: gemini-2.0-flash (Sieu nhanh, tieng Viet chuan, 15 req/phut/key).

pip install openpyxl google-genai
python auto_fill_vocab.py
"""

import os
import sys
import json
import time
import re
import random

os.environ["PYTHONIOENCODING"] = "utf-8"

# Fix: chuyen TEMP sang o E de tranh loi "No space left on device" tren o C
TEMP_DIR = r"e:\ProjectLMSGradution\LMS_Backend\repo-dictionary\temp"
os.makedirs(TEMP_DIR, exist_ok=True)
os.environ["TEMP"] = TEMP_DIR
os.environ["TMP"] = TEMP_DIR

LOG_FILE = os.path.join(
    r"e:\ProjectLMSGradution\LMS_Backend\repo-dictionary", "auto_fill_log.txt"
)
_log_fh = open(LOG_FILE, "a", encoding="utf-8")


def log(msg):
    _log_fh.write(msg + "\n")
    _log_fh.flush()
    try:
        print(msg, flush=True)
    except UnicodeEncodeError:
        print(msg.encode("ascii", errors="replace").decode("ascii"), flush=True)


try:
    from google import genai
    from google.genai import types
except ImportError:
    print("Can cai: pip install google-genai")
    sys.exit(1)

try:
    from openpyxl import load_workbook
except ImportError:
    print("Can cai: pip install openpyxl")
    sys.exit(1)

# ══════════════════════════════════════════════════════════════════
# CẤU HÌNH API & FILE
# ══════════════════════════════════════════════════════════════════

GEMINI_API_KEYS = [
    "REDACTED_GEMINI_KEY_1",
    "REDACTED_GEMINI_KEY_2",
    "REDACTED_GEMINI_KEY_3",
]

BASE_DIR = r"e:\ProjectLMSGradution\LMS_Backend\repo-dictionary"
# File gốc của anh (Lưu ý: script cũ tạo ra Vocabulary_Completed, file này mình gộp lại thành Vocabulary)
INPUT_FILE = os.path.join(BASE_DIR, "Vocabulary_Dictionary.xlsx")
OUTPUT_FILE = os.path.join(BASE_DIR, "Vocabulary.xlsx")  # Em đổi tên cho chuẩn luôn

# Gemini Flash xử lý bối cảnh cực khoẻ, để batch=10 cho nhanh x2
BATCH_SIZE = 10
SAVE_EVERY = 200
# Mỗi API Key phím Free được 15 req/phút -> ~4s 1 req.
# Với 3 pool thì lý thuyết là k cần chờ, nhưng ta an toàn để 2s
SLEEP_BETWEEN = 2.0
MAX_RETRIES = 50
MODEL = "gemini-2.0-flash"

# --- ROUND ROBIN CLIENTS ---
clients = [genai.Client(api_key=key) for key in GEMINI_API_KEYS]
client_idx = 0


def get_next_client():
    global client_idx
    c = clients[client_idx]
    client_idx = (client_idx + 1) % len(clients)
    return c, client_idx


# ══════════════════════════════════════════════════════════════════

C_HANZI = 1
C_PINYIN = 2
C_MEANING = 3
C_EX_CN = 4
C_EX_VI = 5
C_HSK = 6
C_SINGLE = 7
C_COMP = 8
C_AUDIO = 9
C_STROKE = 10
C_ETYM_STORY = 11
C_ETYM_IMG = 12
C_IMG_URL = 13
C_WTYPES = 14
C_EX_PINYIN = 15
C_EX_EN = 16


def cv(ws, r, c):
    v = ws.cell(row=r, column=c).value
    return str(v).strip() if v is not None else ""


def row_needs_work(ws, r):
    hanzi = cv(ws, r, C_HANZI)
    if not hanzi:
        return False

    meaning = cv(ws, r, C_MEANING)
    ex_cn = cv(ws, r, C_EX_CN)
    ex_vi = cv(ws, r, C_EX_VI)
    wtypes = cv(ws, r, C_WTYPES)
    etym_story = cv(ws, r, C_ETYM_STORY)
    hsk = cv(ws, r, C_HSK)
    comp = cv(ws, r, C_COMP)

    # Kiem tra xem ban dich dang la tieng Anh hay chua co
    is_en = meaning.startswith("[EN] ") or (
        meaning and re.match(r'^[a-zA-Z\s,;.\-\'"()/|!?0-9:]+$', meaning)
    )

    # Bat buoc dien tat ca cac the loai truong AI sinh ra.
    # Neu THIEU bat ky truong nao, bat buoc sinh lai.
    if is_en or not ex_cn or not ex_vi or not wtypes or not etym_story or not hsk:
        return True

    # Tu ghep thi bat buoc phai co comp
    is_single = cv(ws, r, C_SINGLE) == "TRUE"
    if not is_single and not comp:
        return True

    return False


def build_prompt(items):
    words = ""
    for i, it in enumerate(items):
        words += (
            f"\n[{i + 1}] Từ: {it['hanzi']} | Pinyin: {it['pinyin']} | "
            f"Từ Điển Gốc: {it['meaning']} | "
            f"Câu Tiếng Anh Có Sẵn: {it['ex_en']} | "
            f"Loại: {'Từ đơn' if it['single'] else 'Từ ghép'}"
        )
    return f"""Bạn là một Chuyên gia Ngôn ngữ học Trung - Việt xuất sắc, một nhà Văn hóa học, đồng thời là một giáo viên dạy tiếng Trung truyền cảm hứng.
Nhiệm vụ của bạn là giải nghĩa và xây dựng nội dung học tập cực kỳ chất lượng, nghệ thuật cho {len(items)} từ vựng sau đây:
{words}

YÊU CẦU BẮT BUỘC (Đọc kỹ và tuân thủ 100%):
1. TRẢ VỀ DUY NHẤT 1 JSON ARRAY CHỨA ĐÚNG {len(items)} OBJECT THEO ĐÚNG THỨ TỰ. KHÔNG GIẢI THÍCH, KHÔNG MARKDOWN. Chỉ xuất JSON thuần.
2. Dịch nghĩa (m): Dịch thuật TỰ NHIÊN sang tiếng Việt. TUYỆT ĐỐI BỎ TIẾNG ANH. Nếu có nhiều nghĩa, bắt buộc phân cách bằng dấu `|`.
3. Câu ví dụ (ec): BẮT BUỘC phải DỰA THEO Ý NGHĨA CỦA "Câu Tiếng Anh Có Sẵn" (nếu có cung cấp) để dịch chuẩn xác nhất sang 1 câu tiếng Trung giao tiếp thực tế, tự nhiên.
4. Dịch câu ví dụ (ev): Dịch câu tiếng Trung ví dụ trên sang tiếng Việt một cách RẤT HAY, văn vẻ, mượt mà có dấu đầy đủ.
5. Pinyin câu ví dụ (ep): Cung cấp pinyin cho câu tiếng Trung ví dụ, GHI RÕ DẤU THANH ĐIỆU (VD: mǎi, shì).
6. Câu chuyện từ nguyên (es): VIẾT 1 CÂU CHUYỆN DÀI, CỰC KỲ HOA MỸ VÀ Ý NGHĨA (khoảng 10 câu văn). Phân tích sâu sắc nguồn gốc (Giáp Cốt/Kim Văn), chiết tự các bộ thủ cấu thành và logic người xưa tạo nên chữ này. Lồng ghép góc nhìn triết lý nhân sinh quan, văn hóa Phương Đông vào để câu chuyện vừa là 1 mẹo học sâu sắc vừa thấm thía ý nghĩa.
7. Cấp HSK (h): Điền 1 số từ 1 đến 9 theo chuẩn HSK 3.0. Nếu từ hiếm không thuộc HSK, BẮT BUỘC ĐIỀN SỐ 9 (mức độ cao nhất), tuyệt đối không để rỗng.
8. Thành phần chữ (c): BẮT BUỘC ĐIỀN cho tất cả các từ, không được để rỗng. 
   - Từ ghép: Tách thành các chữ đơn (VD: 学:xué|生:shēng).
   - Từ đơn: Tách thành các bộ thủ hoặc cấu kiện cấu tạo nên chữ đó (VD: chữ 明 tách thành 日:rì|月:yuè).
9. Loại từ (w): CHỈ ĐƯỢC CHỌN TỪ DANH SÁCH DUY NHẤT NÀY: NOUN, VERB, ADJECTIVE, ADVERB, MEASURE_WORD, PARTICLE, IDIOM, CONJUNCTION, PRONOUN, PREPOSITION, PHRASE, NUMERAL. (Nối bằng | nếu có nhiều).

Cấu trúc mỗi object trong mảng trả về:
{{
  "m": "nghĩa 1 | nghĩa 2",
  "ec": "câu tiếng Trung dịch từ Câu Tiếng Anh",
  "ev": "dịch câu ví dụ sang tiếng Việt tự nhiên",
  "ep": "pinyin của câu ví dụ",
  "ee": "chép lại Câu Tiếng Anh Có Sẵn vào đây",
  "h": 3,
  "c": "thành phần (nếu có)",
  "w": "VERB",
  "es": "Câu chuyện dài khoảng 10 câu, hoa văn ý nghĩa, phân tích triết lý..."
}}"""


def parse_response(text, n):
    text = text.strip()
    text = re.sub(r"^```json\s*", "", text)
    text = re.sub(r"^```\s*", "", text)
    text = re.sub(r"\s*```$", "", text)
    text = text.strip()
    try:
        r = json.loads(text)
        if isinstance(r, list):
            return r
        if isinstance(r, dict):
            return [r]
    except json.JSONDecodeError:
        m = re.search(r"\[.*\]", text, re.DOTALL)
        if m:
            try:
                return json.loads(m.group())
            except json.JSONDecodeError:
                pass
    return None


def call_ai(items, retry=0):
    if retry >= MAX_RETRIES:
        log(
            f"    X Loi NGHIEM TRONG: API that bai lien tuc {MAX_RETRIES} lan. Dung he thong."
        )
        sys.exit(1)

    try:
        client, c_idx = get_next_client()
        prompt = build_prompt(items)

        response = client.models.generate_content(
            model=MODEL,
            contents=prompt,
            config=types.GenerateContentConfig(
                temperature=0.7,
                response_mime_type="application/json",
            ),
        )
        
        # DEBUG: Log response
        # log(f"    DEBUG: AI Response: {response.text[:200]}...")

        res_list = parse_response(response.text, len(items))
        if not res_list or len(res_list) != len(items):
            log(
                f"    ! Loi API: Parsing tra ve {len(res_list) if res_list else 0} words, mong doi {len(items)}. Thu lai..."
            )
            time.sleep(SLEEP_BETWEEN * 2)
            return call_ai(items, retry + 1)
        return res_list
    except Exception as e:
        err_msg = str(e)
        if (
            "429" in err_msg
            or "Resource API" in err_msg
            or "quota" in err_msg.lower()
            or "exhausted" in err_msg.lower()
        ):
            log(f"    ! Rate limit key {c_idx + 1}. Thu lai sau 15s...")
            time.sleep(15)
        else:
            log(f"    ! Loi goi API: {err_msg}")
            time.sleep(SLEEP_BETWEEN * 3)
        return call_ai(items, retry + 1)


import urllib.parse


def generate_audio_url(hanzi):
    """Sử dụng Google Translate TTS cho audio rất trong và ổn định định dạng mpeg."""
    encoded = urllib.parse.quote(hanzi)
    return f"https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=zh-CN&q={encoded}"


def generate_stroke_url(hanzi):
    """Sử dụng UNPKG CDN cho hanzi-writer (rất đáng tin cậy)"""
    char = hanzi[0] if hanzi else ""
    if not char:
        return ""
    return f"https://unpkg.com/hanzi-writer-data@2.0.1/{char}.json"


def generate_etymology_image_url(hanzi):
    """Lấy ảnh chiết tự từ hanzi5 (thông qua CDN hoặc path tĩnh ổn định)
    Chỉ lấy cho từ đơn, loại bỏ link hanziyuan vì hay bị die."""
    char = hanzi[0] if hanzi else ""
    if not char:
        return ""
    encoded = urllib.parse.quote(char)
    # Hanzi5 svg url pattern
    return f"https://www.hanzi5.com/assets/bishun/core/{encoded}.svg"


def fill_static_columns(ws, total):
    updated = 0
    for r in range(2, total + 1):
        hanzi = cv(ws, r, C_HANZI)
        if not hanzi:
            continue

        if not cv(ws, r, C_AUDIO):
            ws.cell(row=r, column=C_AUDIO, value=generate_audio_url(hanzi))
            updated += 1
        if not cv(ws, r, C_STROKE):
            ws.cell(row=r, column=C_STROKE, value=generate_stroke_url(hanzi))
            updated += 1
        if not cv(ws, r, C_ETYM_IMG):
            ws.cell(row=r, column=C_ETYM_IMG, value=generate_etymology_image_url(hanzi))
            updated += 1
    return updated


def write_results(ws, rows, results, items):
    for row_num, res, item in zip(rows, results, items):
        if not res:
            continue
        hanzi = item["hanzi"]
        pinyin = item["pinyin"]

        m = res.get("m", "")
        if m:
            old = cv(ws, row_num, C_MEANING)
            if (
                old.startswith("[EN] ")
                or re.match(r'^[a-zA-Z\s,;.\-\'"()/|!?0-9:]+$', old)
                or not old
            ):
                ws.cell(row=row_num, column=C_MEANING, value=m)
        for key, col in [
            ("ec", C_EX_CN),
            ("ev", C_EX_VI),
            ("ep", C_EX_PINYIN),
            ("ee", C_EX_EN),
        ]:
            val = res.get(key, "")
            if not cv(ws, row_num, col):
                ws.cell(row=row_num, column=col, value=val)

        h = res.get("h", 0)
        if not cv(ws, row_num, C_HSK):
            try:
                ws.cell(row=row_num, column=C_HSK, value=int(h))
            except (ValueError, TypeError):
                ws.cell(row=row_num, column=C_HSK, value=0)

        c = res.get("c", "")
        if not cv(ws, row_num, C_COMP):
            ws.cell(row=row_num, column=C_COMP, value=c)

        w = res.get("w", "")
        if not cv(ws, row_num, C_WTYPES):
            ws.cell(row=row_num, column=C_WTYPES, value=w)

        # Static URLs
        if not cv(ws, row_num, C_AUDIO):
            ws.cell(row=row_num, column=C_AUDIO, value=generate_audio_url(hanzi))
        if not cv(ws, row_num, C_STROKE):
            ws.cell(row=row_num, column=C_STROKE, value=generate_stroke_url(hanzi))
        if not cv(ws, row_num, C_ETYM_IMG):
            ws.cell(
                row=row_num,
                column=C_ETYM_IMG,
                value=generate_etymology_image_url(hanzi),
            )

        # Etymology story: Chỉ lưu kết quả từ AI, bắt buộc
        es = res.get("es", "")
        if not cv(ws, row_num, C_ETYM_STORY):
            ws.cell(row=row_num, column=C_ETYM_STORY, value=es)


def main():
    if os.path.exists(OUTPUT_FILE):
        log(f"Tiep tuc tu file da luu: {OUTPUT_FILE}")
        wb = load_workbook(OUTPUT_FILE)
    elif os.path.exists(INPUT_FILE):
        log(f"Bat dau tu file goc: {INPUT_FILE}")
        wb = load_workbook(INPUT_FILE)
    else:
        log("Khong tim thay file! Chay generate_full_dictionary.py truoc.")
        sys.exit(1)

    ws = wb.active
    total = ws.max_row
    log(f"Tong: {total} dong (header + {total - 1} tu)")

    # Dien nhanh cac cot tinh truoc khi goi AI.
    static_updated = fill_static_columns(ws, total)
    if static_updated > 0:
        wb.save(OUTPUT_FILE)
        log(f"Da dien cot tinh (audio/stroke/etymology*) cho {static_updated} o.")

    todo = []
    for r in range(2, total + 1):
        if row_needs_work(ws, r):
            todo.append(r)

    need = len(todo)
    log(f"Can xu ly: {need} tu")

    if need == 0:
        log("Tat ca da duoc xu ly!")
        return

    batches = (need + BATCH_SIZE - 1) // BATCH_SIZE
    est = batches * SLEEP_BETWEEN / 60
    log(f"Uoc tinh: {batches} batch x {BATCH_SIZE} tu ~ {est:.0f} phut")
    log(f"Groq model: {MODEL}")
    log("=" * 60)

    done = 0
    items = []
    rows = []

    for row_num in todo:
        hanzi = cv(ws, row_num, C_HANZI)
        pinyin = cv(ws, row_num, C_PINYIN)
        meaning = cv(ws, row_num, C_MEANING)
        ex_en = cv(ws, row_num, C_EX_EN)
        single = cv(ws, row_num, C_SINGLE) == "TRUE"

        items.append(
            {
                "hanzi": hanzi,
                "pinyin": pinyin,
                "meaning": meaning,
                "ex_en": ex_en,
                "single": single,
            }
        )
        rows.append(row_num)

        if len(items) >= BATCH_SIZE or row_num == todo[-1]:
            bn = (done // BATCH_SIZE) + 1
            log(f"\nBatch {bn}/{batches} [{done + 1}-{done + len(items)}/{need}]")

            results = call_ai(items)
            if results:
                write_results(ws, rows, results, items)
                log(f"  OK {len(results)} tu")
            else:
                log(f"  FAIL, bo qua")

            done += len(items)
            items = []
            rows = []

            if done % SAVE_EVERY == 0 or row_num == todo[-1]:
                wb.save(OUTPUT_FILE)
                pct = done / need * 100
                log(f"  SAVED ({done}/{need} = {pct:.1f}%)")
                break  # EXITED EARLY FOR 10 WORD TEST

            if row_num != todo[-1]:
                time.sleep(SLEEP_BETWEEN + random.uniform(0.3, 1.5))

    wb.save(OUTPUT_FILE)
    log(f"\n{'=' * 60}")
    log(f"HOAN THANH! Da xu ly {done} tu")
    log(f"File: {OUTPUT_FILE}")
    log(f"{'=' * 60}")


if __name__ == "__main__":
    main()
