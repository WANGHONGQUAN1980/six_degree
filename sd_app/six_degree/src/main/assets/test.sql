SELECT g0._id,g0.destroyed,g0.name,g0.owner as ownerId,
            g1.member_count as group_count ,g1.nickName,g1.time as lastTime,g1.messageJson as lastContent,g1.eventType as lastEventType,
            g2.newMessageCount as newCount,g2.newToMeMessageCount>0 as hasToMe,g2.messageCount as count,containsType FROM 
            (
                SELECT DISTINCT mg._id,mg.destroyed,mg.owner,mg.name,(CASE WHEN name LIKE "" THEN 'NAME' ELSE 'CONTENT' END) AS containsType
                FROM (SELECT * FROM sd_message_group WHERE _id IN ("1","0","535761e21801b518bc73708d5d07427f","6537db4923acc558d63cd8391ceb9e43","16fcf48fb042903b9d580aec7f4e5d0d")) AS mg
                LEFT JOIN (SELECT * FROM sd_messages WHERE `to` IN ("1","0","535761e21801b518bc73708d5d07427f","6537db4923acc558d63cd8391ceb9e43","16fcf48fb042903b9d580aec7f4e5d0d") ) AS m ON mg._id = m.`to`
                WHERE name LIKE "" OR m.messageJson LIKE ""
                ORDER BY (CASE WHEN _order IS NULL THEN 0 ELSE _order END) DESC, name
            ) AS g0
            LEFT OUTER JOIN
            (
                SELECT tmp._id,tmp.member_count ,mm.nickName,mm.time,mm.messageJson,mm.eventType
                FROM (
                    SELECT gm._id,max(m.time) as max_time,member_count
                    FROM
                        (SELECT _id,count(distinct mobile) as member_count
                        FROM sd_group_members
                        WHERE _id IN ("1","0","535761e21801b518bc73708d5d07427f","6537db4923acc558d63cd8391ceb9e43","16fcf48fb042903b9d580aec7f4e5d0d")
                        GROUP BY _id) AS gm
                    LEFT OUTER  JOIN
                    sd_messages AS m
                    ON m.`to` = gm._id
                    GROUP BY gm._id) as tmp
                LEFT OUTER JOIN sd_messages AS mm
                ON tmp._id = mm.`to` AND tmp.max_time = mm.time
            ) as g1
            ON g0._id = g1._id
            LEFT OUTER JOIN 
            (
                SELECT m.`to` AS groupId,
                SUM(
                    CASE 
                        WHEN m.time>ifnull(r.lastRead,0) THEN 1
                        ELSE 0
                    END
                ) AS newMessageCount,
                SUM(
                    CASE 
                        WHEN m.time>ifnull(r.lastRead,0) AND m.messageJson LIKE "%@贾宝玉%" THEN 1
                        ELSE 0
                    END
                ) AS newToMeMessageCount,
                COUNT(1) AS messageCount
                FROM (SELECT `to`,time,messageJson FROM sd_messages WHERE `to` IN ("1","0","535761e21801b518bc73708d5d07427f","6537db4923acc558d63cd8391ceb9e43","16fcf48fb042903b9d580aec7f4e5d0d")) AS m
                LEFT OUTER JOIN (SELECT lastRead,sr.groupId FROM sd_read AS sr WHERE sr.groupId IN ("1","0","535761e21801b518bc73708d5d07427f","6537db4923acc558d63cd8391ceb9e43","16fcf48fb042903b9d580aec7f4e5d0d") AND mobile="10000000028") AS r ON m.`to` = r.groupId
                GROUP BY m.`to`
            ) AS g2
            ON g1._id = g2.groupId