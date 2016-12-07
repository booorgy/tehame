CREATE PROCEDURE createusers(p1 INT)
BEGIN
usersloop: LOOP
	SET p1 = p1 + 1;
		IF p1 < 1000000 THEN
			INSERT INTO user VALUES (RAND()*RAND(),RAND()*RAND(),"a");
      ITERATE usersloop;
    END IF;
		LEAVE usersloop;
SET @x = p1;
END LOOP usersloop;
END;