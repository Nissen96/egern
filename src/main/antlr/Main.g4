grammar Main;

prog:	( stmt | funcDecl | funcCall ';'? )* ;
stmt:   varDecl
    |   varAssign
    |   opAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    |   whileLoop
    ;

returnStmt: 'return' expr? ';'? ;
funcDecl:   'func' ID '(' paramList ')' ':' typeDecl '{' funcBody '}'  ;
funcBody:   ( stmt | funcDecl | funcCall ';'? )* ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ':' typeDecl ',')* (ID ':' typeDecl)? ;
argList:    (expr ',')* expr? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';'?;
opAssign: ID op=('+=' | '-=' | '*=' | '/=' | '%=' ) expr ';'?;

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
    | arrayExpr
    | parenExpr
    ;

idExpr: ID ;
intExpr: INT ;
booleanExpr: BOOLEAN ;
parenExpr: '(' expr ')';
arrayExpr: '[' (expr ',')* expr? ']';

typeDecl: VOID | PRIMITIVE | arrayType;
arrayType: '[' (arrayType | PRIMITIVE) ']';

printStmt: 'print' '(' expr? ')' ';'?;

NEWLINE  :'\r'? '\n' -> skip;
WS       : (' '|'\t') -> skip;
INT      : [0-9]+ ;
BOOLEAN  : 'true' | 'false' ;
VOID     : 'void' ;
PRIMITIVE: 'int' | 'boolean' ;
ID       : [A-Za-z_]+[A-Za-z_0-9]* ;
