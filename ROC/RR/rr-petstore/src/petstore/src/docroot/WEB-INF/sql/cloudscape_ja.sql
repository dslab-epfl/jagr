DELETE FROM item_ja;
DELETE FROM product_ja;
DELETE FROM category_ja;

INSERT INTO account VALUES('j2ee-���{', 'yourname@yourdomain.com','The', 'Duke', 'OK', '��c1-10-1',
'', '�`��', '�����s', '��107-0000', 'Japan', '03-5555-5555')
;
INSERT INTO signon VALUES('j2ee-���{','j2ee-���{')
;
INSERT INTO profile VALUES('j2ee-���{','Japanese','fish','1','1')
;
INSERT INTO category_ja VALUES ('FISH','��','<image src="../images/fish_icon.gif"><font size="5"color="blue"> ��</font>');
INSERT INTO category_ja VALUES ('DOGS','��','<image src="../images/dogs_icon.gif"><font size="5"color="blue"> ��</font>');
INSERT INTO category_ja VALUES ('REPTILES','঒���','<image src="../images/reptiles_icon.gif"><font size="5"color="blue"> ঒���</font>');
INSERT INTO category_ja VALUES ('CATS','�L','<image src="../images/cats_icon.gif"><font size="5"color="blue"> �L</font>');
INSERT INTO category_ja VALUES ('BIRDS','��','<image src="../images/birds_icon.gif"><font size="5" color="blue"> ��</font>');


INSERT INTO product_ja VALUES ('FI-SW-01','FISH','�G���[���t�B�b�V��','<image src="../images/fish1.jpg"> �I�[�X�g�����A�Y�̊C����');
INSERT INTO product_ja VALUES ('FI-SW-02','FISH','�C�^�`�U��','<image src="../images/fish4.gif"> �I�[�X�g�����A�Y�̊C����');
INSERT INTO product_ja VALUES ('FI-FW-01','FISH', '��','<image src="../images/fish3.gif"> ���{�Y�̒W����');
INSERT INTO product_ja VALUES ('FI-FW-02','FISH', '����','<image src="../images/fish2.gif"> �����Y�̒W����');


INSERT INTO product_ja VALUES ('K9-BD-01','DOGS','�u���h�b�O','<image src="../images/dog2.gif"> �l�Ȃ����������A�C�M���X�Y');
INSERT INTO product_ja VALUES ('K9-PO-02','DOGS','�v�[�h��','<image src="../images/dog6.gif"> �L���[�g�ȃt�����X�Y�̌�');
INSERT INTO product_ja VALUES ('K9-DL-01','DOGS', '�_���}�V�A��','<image src="../images/dog5.gif"> ���h���ő劈��');
INSERT INTO product_ja VALUES ('K9-RT-01','DOGS', '�S�[���f��=���g���[�o','<image src="../images/dog1.gif"> �������ɍœK');
INSERT INTO product_ja VALUES ('K9-RT-02','DOGS', '���u���h��=���g���[�o','<image src="../images/dog5.gif"> ���ɍœK');
INSERT INTO product_ja VALUES ('K9-CW-01','DOGS', '�`����','<image src="../images/dog4.gif"> �ƂĂ��l�Ȃ���');


INSERT INTO product_ja VALUES ('RP-SN-01','REPTILES','�K���K����','<image src="../images/lizard3.gif"> �Ԍ�����ɂǂ���');
INSERT INTO product_ja VALUES ('RP-LI-02','REPTILES','�C�O�A�i','<image src="../images/lizard2.gif"> �l�Ȃ����ΐF�̂��F�B');
INSERT INTO product_ja VALUES ('FL-DSH-01','CATS','�}���N�X�l�R','<image src="../images/cat3.gif"> �˂��ݑގ��ɍœK');
INSERT INTO product_ja VALUES ('FL-DLH-02','CATS','�y���V�A�l�R','<image src="../images/cat1.gif"> �v�����Z�X�݂����ȁA�D�����ƃl�R');
INSERT INTO product_ja VALUES ('AV-CB-01','BIRDS','�{�E�V�C���R','<image src="../images/bird4.gif"> �ō��� 75 �N��������');
INSERT INTO product_ja VALUES ('AV-SB-02','BIRDS','�t�B���`','<image src="../images/bird1.gif"> �����n�y�b�g');

INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-1','FI-SW-01',11650,10.00,1,'P','��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-2','FI-SW-01',11650,10.00,1,'P','��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-3','FI-SW-02',18350,12.00,1,'P','�����Ȃ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-4','FI-FW-01',18350,12.00,1,'P','�܂���');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-5','FI-FW-01',14850,12.00,1,'P','�܂���Ȃ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-6','K9-BD-01',14950,1200,1,'P','�Y ���Ƃ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-7','K9-BD-01',18250,1200,1,'P','�� �q��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-8','K9-PO-02',18150,1200,1,'P','�Y �q��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-9','K9-DL-01',10850,1200,1,'P','�܂���Ȃ� �Y �q��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-10','K9-DL-01',10850,1200,1,'P','�܂��� ���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-11','RP-SN-01',2050,1200,1,'P','�łȂ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-12','RP-SN-01',1850,1200,1,'P','���Ȃ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-13','RP-LI-02',1950,1200,1,'P','�� ���Ƃ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-14','FL-DSH-01',5850,1200,1,'P','�����ۂȂ�');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-15','FL-DSH-01',2350,1200,1,'P','�����ۂ���');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-16','FL-DLH-02',9350,1200,1,'P','���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-17','FL-DLH-02',9350,1200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-18','AV-CB-01',19350,9200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-19','AV-SB-02',14550, 200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-20','FI-FW-02',550, 200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-21','FI-FW-02',529, 100,1,'P','���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-22','K9-RT-02',13550, 10000,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-23','K9-RT-02',14549, 10000,1,'P','���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-24','K9-RT-02',25550, 9200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-25','K9-RT-02',32529, 9000,1,'P','���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-26','K9-CW-01',12550, 9200,1,'P','���Ƃ� �Y');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-27','K9-CW-01',15529, 9000,1,'P','���Ƃ� ��');
INSERT INTO  item_ja (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES ('EST-28','K9-RT-01',15529, 9000,1,'P','���Ƃ� ��');
