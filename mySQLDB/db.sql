CREATE TABLE `my_splits` (
  `splitted_column` varchar(500) NOT NULL,
  `id` int(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


CREATE TABLE `tbl_chathistory` (
  `Account_id` int(11) DEFAULT NULL,
  `User_id` text,
  `Chat_Group` text,
  `Chat` text,
  `Latitude` double DEFAULT NULL,
  `Longitude` double DEFAULT NULL,
  `chat_ts` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbl_finallist` (
  `splitted_column` varchar(500) DEFAULT NULL,
  `id` varchar(45) DEFAULT NULL,
  `cnt` int(11) DEFAULT NULL,
  `insert_ts` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbl_stopwordlist` (
  `wordslist` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tbl_userinventory` (
  `User_id` text,
  `Account_id` int(11) DEFAULT NULL,
  `Name` text,
  `Age` int(11) DEFAULT NULL,
  `State` text,
  `Gender` text,
  `Sports` int(11) DEFAULT NULL,
  `Business` int(11) DEFAULT NULL,
  `Books` int(11) DEFAULT NULL,
  `Politics` int(11) DEFAULT NULL,
  `Art` int(11) DEFAULT NULL,
  `Photography` int(11) DEFAULT NULL,
  `Design` int(11) DEFAULT NULL,
  `Technology` int(11) DEFAULT NULL,
  `Music` int(11) DEFAULT NULL,
  `Travel` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `split_string`()
BEGIN

DECLARE my_delimiter CHAR(1);
DECLARE split_string varchar(5000);
DECLARE done INT;
DECLARE occurance INT;
DECLARE i INT;
DECLARE split_id INT;
DECLARE ins_query VARCHAR(5000);
DECLARE splitter_cur CURSOR FOR
SELECT account_id,replace(chat,'''','') chat FROM vgroup.tbl_chathistory;


DECLARE CONTINUE HANDLER FOR NOT FOUND SET done=1;

TRUNCATE  TABLE`my_splits`;


OPEN splitter_cur;
splitter_loop:LOOP
      FETCH splitter_cur INTO split_id,split_string;

SET occurance=length(split_string)-length(replace(split_string,' ',''))+1;
SET my_delimiter=' ';
  IF done=1 THEN
    LEAVE splitter_loop;
  END IF;
#  select occurance;
  IF occurance > 0 then
    #select occurance;
    set i=1;
    while i <= occurance do
#        select concat("SUBSTRING_INDEX(SUBSTRING_INDEX( '",split_string ,"', '",my_delimiter,"', ",i, "),'",my_delimiter,"',-1);");
insert into my_splits (`splitted_column`,`id`)
SELECT SPLIT_STR1(split_string, ' ', i) as splitted_column,split_id;
        SET ins_query=concat("insert into my_splits(splitted_column,id) values(", concat("SUBSTRING_INDEX(SUBSTRING_INDEX( '",split_string ,"', '",my_delimiter,"', ",i, "),'",my_delimiter,"',-1),",split_id,");"));
        
    #select ins_query;
        set @ins_query=ins_query;
        #select @ins_query;
        PREPARE ins_query from @ins_query;
        EXECUTE ins_query;
      set i=i+1;
    end while;
  ELSE
        set ins_query=concat("insert into my_splits(splitted_column,id) values(",split_string,"',",split_id,");");
        set @ins_query=ins_query;
        PREPARE ins_query from @ins_query;
        EXECUTE ins_query;
  END IF;
  set occurance=0;
END LOOP;

CLOSE splitter_cur;

TRUNCATE TABLE tbl_finallist;

 insert into tbl_finallist
 (splitted_column,id,cnt)
select  
lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(splitted_column,'!',''),'"',''),'.',''),'.',''),')',''),'(',''),'/',''),'?',''),'&',''),'\\',''),'+',''),'-',''),'<','')) as splitted_column 
 ,id,count(*) as cnt from my_splits where splitted_column is not null and splitted_column <> ''
and splitted_Column not in (select wordslist from tbl_stopwordlist)
group by lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(splitted_column,'!',''),'"',''),'.',''),'.',''),')',''),'(',''),'/',''),'?',''),'&',''),'\\',''),'+',''),'-',''),'<','') ),id
 
order by cnt desc
limit 1;

insert into tbl_finallist
(splitted_column,id,cnt)
select  
lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(splitted_column,'!',''),'"',''),'.',''),'.',''),')',''),'(',''),'/',''),'?',''),'&',''),'\\',''),'+',''),'-',''),'<','')) as splitted_column 
 ,'NFL',count(*) as cnt from my_splits where splitted_column is not null and splitted_column <> ''
and splitted_Column not in (select wordslist from tbl_stopwordlist)
group by lower(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(splitted_column,'!',''),'"',''),'.',''),'.',''),')',''),'(',''),'/',''),'?',''),'&',''),'\\',''),'+',''),'-',''),'<',''))  
 
order by cnt desc
limit 1;


END$$
DELIMITER ;



DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `SPLIT_STR1`(
  x VARCHAR(255),
  delim VARCHAR(12),
  pos INT
) RETURNS varchar(255) CHARSET utf8
RETURN REPLACE(SUBSTRING(SUBSTRING_INDEX(x, delim, pos),
       LENGTH(SUBSTRING_INDEX(x, delim, pos -1)) + 1),
       delim, '')$$
DELIMITER ;
