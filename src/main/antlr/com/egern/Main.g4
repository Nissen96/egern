grammar Main;

@header {
package com.egern.antlr;
}

hello: 'hello' ID ;
ID   : [a-zA-Z]+ ;
WS   : [ \r\n\t]+ -> skip ;
