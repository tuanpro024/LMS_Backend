USE lms_flashcard_db;

-- Delete old showcase data to avoid duplicates
DELETE FROM vocabulary_meanings WHERE vocabulary_id IN (SELECT id FROM vocabularies WHERE hanzi IN ('学', '习', '学习'));
DELETE FROM vocabulary_components WHERE parent_vocab_id IN (SELECT id FROM vocabularies WHERE hanzi = '学习');
DELETE FROM vocabularies WHERE hanzi IN ('学', '习', '学习');

-- 1. Insert '学' (Component)
INSERT INTO vocabularies (hsk_level, word_type, hanzi, pinyin, stroke_animation_url, etymology_story, is_single_vocab, deleted, created_at, updated_at) 
VALUES (1, 'Động từ', '学', 'xué', 'https://hanzi-writer.org/images/strokes/5b66.svg', 'Chữ Học gồm bộ Tử (đứa trẻ) dưới mái nhà, thể hiện sự giáo dục.', TRUE, FALSE, NOW(), NOW());
SET @id_xue = LAST_INSERT_ID();

INSERT INTO vocabulary_meanings (vocabulary_id, meaning, deleted, created_at, updated_at) 
VALUES (@id_xue, 'Học, bắt chước', FALSE, NOW(), NOW());

-- 2. Insert '习' (Component)
INSERT INTO vocabularies (hsk_level, word_type, hanzi, pinyin, stroke_animation_url, etymology_story, is_single_vocab, deleted, created_at, updated_at) 
VALUES (1, 'Động từ', '习', 'xí', 'https://hanzi-writer.org/images/strokes/4e60.svg', 'Hình ảnh đôi cánh chim đang đập cánh luyện tập bay trên bầu trời.', TRUE, FALSE, NOW(), NOW());
SET @id_xi = LAST_INSERT_ID();

INSERT INTO vocabulary_meanings (vocabulary_id, meaning, deleted, created_at, updated_at) 
VALUES (@id_xi, 'Luyện tập, thói quen', FALSE, NOW(), NOW());

-- 3. Insert '学习' (Compound)
INSERT INTO vocabularies (hsk_level, word_type, hanzi, pinyin, audio_url, image_url, etymology_story, etymology_image, is_single_vocab, deleted, created_at, updated_at) 
VALUES (1, 'Động từ', '学习', 'xuéxí', 'https://www.chinesereadingpractice.com/wp-content/uploads/2012/11/xue2xi2.mp3', 'file:///C:/Users/ADMIN/.gemini/antigravity/brain/53542fea-3b5b-4f36-9a15-595d723f328f/learning_main_illustration_1769533150826.png', 'Học phải đi đôi với hành. Chữ Học (学) là hình ảnh đứa trẻ học dưới mái nhà, Tập (习) là đôi cánh chim non đang tập bay.', 'file:///C:/Users/ADMIN/.gemini/antigravity/brain/53542fea-3b5b-4f36-9a15-595d723f328f/learning_etymology_image_1769533169761.png', FALSE, FALSE, NOW(), NOW());
SET @id_xuexi = LAST_INSERT_ID();

INSERT INTO vocabulary_meanings (vocabulary_id, meaning, example_sentence_cn, example_sentence_vi, example_sentence_en, example_sentence_pinyin, deleted, created_at, updated_at) 
VALUES (@id_xuexi, 'Học tập, nghiên cứu kiến thức mới', '我在这所大学学习已经三年了。', 'Tôi đã học ở trường đại học này được 3 năm rồi.', 'I have been studying at this university for three years.', 'Wǒ zài zhè suǒ dàxué xuéxí yǐjīng sān nián le.', FALSE, NOW(), NOW());

INSERT INTO vocabulary_meanings (vocabulary_id, meaning, example_sentence_cn, example_sentence_vi, example_sentence_en, example_sentence_pinyin, deleted, created_at, updated_at) 
VALUES (@id_xuexi, 'Noi gương, học hỏi từ người khác', '我们要向他学习这种刻苦钻研的精神。', 'Chúng ta cần học tập tinh thần nghiên cứu khổ cực của anh ấy.', 'We should learn from his spirit of diligent research.', 'Wǒmen yào xiàng tā xuéxí zhè zhǒng kèkǔ zuānyán de jīngshén.', FALSE, NOW(), NOW());

-- 4. Link components
INSERT INTO vocabulary_components (parent_vocab_id, component_vocab_id, order_index, deleted, created_at, updated_at) 
VALUES (@id_xuexi, @id_xue, 1, FALSE, NOW(), NOW());
INSERT INTO vocabulary_components (parent_vocab_id, component_vocab_id, order_index, deleted, created_at, updated_at) 
VALUES (@id_xuexi, @id_xi, 2, FALSE, NOW(), NOW());
