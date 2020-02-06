grammar Main;

prog:	(expr NEWLINE)* ;
expr:	expr op=('*'|'/') expr
    |	expr op=('+'|'-') expr
    |	INT
    |	'(' expr ')'
    ;
NEWLINE : [\r\n]+ ;
INT     : [0-9]+ ;