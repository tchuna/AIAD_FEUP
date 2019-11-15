#! /bin/sh

java -cp "./jade.jar":. jade.Boot -gui -agents " Auctionner:AuctioneerAg;Ana:BidderAgent(1000,1);Pedro:BidderAgent(500,4);Jorge:BidderAgent(300,5);Hugo:BidderAgent(800,3);Maria:BidderAgent(150,4);Tchuna:BidderAgent(620,2)"
