SELECT count(*)
FROM User
WHERE user_id = /*userId*/1
GROUP BY user_id;