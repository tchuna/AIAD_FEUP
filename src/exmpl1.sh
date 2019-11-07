#! /bin/sh

java -cp "./jade.jar":. jade.Boot -gui -agents " Auctionner:AuctioneerAg;Ana:BidderAgent;Pedro:BidderAgent;Jorge:BidderAgent;Hugo:BidderAgent"
