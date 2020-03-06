grammar Main;

prog:	( stmt | funcDecl | funcCall ';'? )* ;
stmt:   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    |   whileLoop
    ;

returnStmt: 'return' expr? ';'? ;
funcDecl:   'func' ID '(' paramList ')' ':' TYPE '{' funcBody '}'  ;
funcBody:   ( stmt | funcDecl | funcCall ';'? )* ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ':' TYPE ',')* (ID ':' TYPE)? ;
argList:    (expr ',')* expr? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';'?;

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' (block | ifElse)
      ;

whileLoop: 'while' '(' expr ')' block ;

block:  '{' ( stmt | funcCall ';'? )* '}' ;

expr: funcCall
    | op='-' expr
    | op='!' expr
    | expr op=('*' | '/' | '%') expr
    | expr op=('+' | '-') expr
    | expr op=('==' | '!=' | '<' | '>' | '<=' | '>=') expr
    | expr op='&&' expr
    | expr op='||' expr
    | booleanExpr
    | idExpr
    | intExpr
    | parenExpr
    ;

idExpr: ID ;
intExpr: INT ;
booleanExpr: BOOLEAN ;
parenExpr: '(' expr ')';

printStmt: 'print' '(' expr? ')' ';'?;

NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
BOOLEAN : 'true' | 'false' ;
TYPE    : 'int' | 'boolean' | 'void' ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;
