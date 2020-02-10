grammar Main;

prog:	(stmt | funcDecl)* ;
stmt:   funcCall
    |   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    ;

returnStmt: 'return' expr? ';' ;
funcDecl:   'func' ID '(' paramList ')' block ;
funcCall:   ID '(' argList ')' ';' ;

paramList:  (ID ',')* ID? ;
argList:    (ID ',')* ID? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ (expr ';' | funcCall) ;

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' block
      ;

block:  '{' stmt* '}' ;

expr: compExpr | arithExpr ;

compExpr: arithExpr op=('==' | '!=' | '<' | '>' | '<=' | '>=') arithExpr;

arithExpr:	arithExpr op=('*'|'/') arithExpr
    |	arithExpr op=('+'|'-') arithExpr
    |	INT
    |   ID
    |	'(' arithExpr ')'
    ;
NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;