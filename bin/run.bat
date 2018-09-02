@echo off
set ASPECTWERKZ_HOME=..
aspectwerkz -cp ../lib/damon.jar;../lib/easypastry.jar;../lib/log4j.jar;../lib/asm-all-3.1.jar;../lib/pastry.jar;../lib/bunshin.jar;../lib/junit.jar;../lib/jdom.jar;../lib/xstream.jar;../lib/sbbi-upnplib-1.0.4.jar; %*
