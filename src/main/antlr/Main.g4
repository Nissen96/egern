grammar Main;

prog:	(stmt | funcDecl | funcCall ';')* ;
stmt:   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    ;

returnStmt: 'return' expr? ';' ;
funcDecl:   'func' ID '(' paramList ')' block ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ',')* ID? ;
argList:    (expr ',')* expr? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';';

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' block
      ;

block:  '{' (stmt | funcCall)* '}' ;

expr: compExpr | arithExpr | funcCall ;

compExpr: arithExpr op=('==' | '!=' | '<' | '>' | '<=' | '>=') arithExpr;

arithExpr:	arithExpr op=('*'|'/') arithExpr
    |	arithExpr op=('+'|'-') arithExpr
    |	INT
    |   ID
    |	'(' arithExpr ')'
    ;

printStmt: 'print' '(' expr? ')' ';';

NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;