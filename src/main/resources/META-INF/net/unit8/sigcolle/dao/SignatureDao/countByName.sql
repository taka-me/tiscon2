SELECT count(*)
FROM Signature
WHERE name = /*name*/1 AND campaign_id =/*camId*/1
GROUP BY name;