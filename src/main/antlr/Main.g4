grammar Main;

prog:	( classDecl | stmt | funcDecl | funcCall ';'? )* ;
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

assignable: idExpr
          | arrayIndexExpr ;

varDecl: 'var' (ID '=')+ expr ';'? ;
varAssign: (assignable '=')+ expr ';'?;
opAssign: assignable op=('+=' | '-=' | '*=' | '/=' | '%=' ) expr ';'?;

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' (block | ifElse)
      ;

whileLoop: 'while' '(' expr ')' block ;

block:  '{' ( stmt | funcCall ';'? )* '}' ;

classDecl: 'class' ID '{' classBody '}' ;
classBody: (funcDecl | varDecl)* ;

methodCall: ID '.' funcCall ;
classField: ID '.' ID ;

arrayIndexExpr: idExpr ('[' expr ']')+ ;

expr: funcCall
    | methodCall
    | classField
    | arrayIndexExpr
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
    | lenExpr
    ;

idExpr: ID ;
intExpr: INT ;
booleanExpr: BOOLEAN ;
parenExpr: '(' expr ')';
arrayExpr: '[' (expr ',')* expr? ']';
lenExpr: 'len' '(' expr ')';

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
