DELETE FROM ids;

#
# This code doesn't work properly
#
# INSERT INTO ids (category,region,users,item,comment,bid,buyNow) SELECT MAX(categories.id)+1,MAX(regions.id)+1,MAX(users.id)+1,MAX(items.id)+1,MAX(comments.id)+1,MAX(bids.id)+1,MAX(buy_now.id)+1 FROM categories,regions,users,items,comments,bids,buy_now;
# 

INSERT INTO ids (category,region) SELECT MAX(categories.id)+1, MAX(regions.id)+1 from categories, regions;
UPDATE ids SET users = 0, item = 0, comment = 0, bid = 0, buyNow = 0;
