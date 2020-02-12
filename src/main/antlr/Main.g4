grammar Main;

prog:	(stmt | funcDecl)* ;
stmt:   funcCall
    |   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    ;

returnStmt: 'return' exprOrFuncCall? ';' ;
funcDecl:   'func' ID '(' paramList ')' block ;
funcCall:   ID '(' argList ')' ';' ;

paramList:  (ID ',')* ID? ;
argList:    (ID ',')* ID? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ exprOrFuncCall;

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' block
      ;

block:  '{' stmt* '}' ;

expr: compExpr | arithExpr ;
exprOrFuncCall: (expr ';' | funcCall) ;

compExpr: arithExpr op=('==' | '!=' | '<' | '>' | '<=' | '>=') arithExpr;

arithExpr:	arithExpr op=('*'|'/') arithExpr
    |	arithExpr op=('+'|'-') arithExpr
    |	INT
    |   ID
    |	'(' arithExpr ')'
    ;

printStmt: 'print' '(' exprOrFuncCall ')' ';';

NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;