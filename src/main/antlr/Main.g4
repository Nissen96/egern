grammar Main;

prog:	(stmt | funcDecl)* ;
stmt:   funcCall
    |   varDecl
    |   varAssign
    |   ifElse
    ;

funcDecl:   'func' ID '(' (ID ',')* ID? ')' block ;
funcCall:   ID '(' (ID ',')* ID? ')' ';' ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';' ;

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' block
      ;

block:  '{' stmt* '}' ;

expr: compExpr | arithExpr ;

compExpr: comparable op=('==' | '!=' | '<' | '>' | '<=' | '>=') comparable;
comparable: arithExpr | ID ;

arithExpr:	arithExpr op=('*'|'/') arithExpr
    |	arithExpr op=('+'|'-') arithExpr
    |	INT
    |   ID
    |	'(' arithExpr ')'
    ;
NEWLINE : [\r\n]+ ;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;