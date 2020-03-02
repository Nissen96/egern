grammar Main;

prog:	( stmt | funcDecl | funcCall ';' )* ;
stmt:   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    |   whileLoop
    ;

returnStmt: 'return' expr? ';' ;
funcDecl:   'func' ID '(' paramList ')' '{' funcBody '}' ;
funcBody:   ( stmt | funcDecl | funcCall ';' )* ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ',')* ID? ;
argList:    (expr ',')* expr? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';';

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' (block | ifElse)
      ;

whileLoop: 'while' '(' expr ')' block ;

block:  '{' ( stmt | funcCall ';' )* '}' ;

expr: funcCall
    | '-' expr
    | expr op=('*' | '/' | '%') expr
    | expr op=('+' | '-') expr
    | expr op=('==' | '!=' | '<' | '>' | '<=' | '>=') expr
    | idExpr
    | intExpr
    | parenExpr
    ;

idExpr: ID ;
intExpr: INT ;
parenExpr: '(' expr ')';

printStmt: 'print' '(' expr? ')' ';';

NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;