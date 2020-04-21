grammar Main;

prog:   ( stmt | funcDecl | classDecl )* ;
stmt:   varDecl ';'?
    |   varAssign ';'?
    |   opAssign ';'?
    |   ifElse
    |   returnStmt ';'?
    |   printStmt ';'?
    |   whileLoop
    |   funcCall ';'?
    |   methodCall ';'?
    ;

varDecl: 'var' (ID '=')+ expr ;
varAssign: (assignable '=')+ expr ;
opAssign: assignable op=('+=' | '-=' | '*=' | '/=' | '%=' ) expr ;
assignable: idExpr
          | arrayIndexExpr
          | classField
          ;

ifElse: 'if' '(' expr ')' block ('else' (block | ifElse))? ;
returnStmt: 'return' expr? ;
printStmt: 'print' '(' expr? ')' ;

whileLoop: 'while' '(' expr ')' block ;
funcCall:   ID '(' argList ')' ;
argList:    (expr ',')* expr? ;

funcDecl:   'func' ID '(' paramList ')' (':' typeDecl)? '{' funcBody '}' ;
paramList:  (ID ':' typeDecl ',')* (ID ':' typeDecl)? ;
funcBody:   ( stmt | funcDecl )* ;

block:  '{' stmt* '}' ;

classDecl: 'class' CLASSNAME ('(' paramList ')')? (':' CLASSNAME ('(' argList ')')?)? '{' classBody '}' ;
classBody: ( methodDecl | fieldDecl )* ;

methodDecl: MODIFIER* funcDecl ;
fieldDecl: MODIFIER* varDecl ;
methodCall: (CLASSNAME | ID | 'this') '.' funcCall ;
classField: (CLASSNAME | ID | 'this') '.' ID ;
objectInstantiation: CLASSNAME '(' argList ')' ;

arrayIndexExpr: indexable ('[' expr ']')+ ;
indexable: (idExpr | classField | funcCall | methodCall) ;

expr: funcCall
    | objectInstantiation
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
    | stringExpr
    | arrayExpr
    | parenExpr
    | lenExpr
    | expr 'as' typeDecl
    ;

idExpr: ID ;
intExpr: INT ;
booleanExpr: BOOLEAN ;
stringExpr: STRING ;
parenExpr: '(' expr ')';
arrayExpr: '[' (expr ',')* expr? ']';
lenExpr: 'len' '(' expr ')';

typeDecl: VOID | PRIMITIVE | arrayType | CLASSNAME ;
arrayType: '[' (arrayType | PRIMITIVE) ']';

NEWLINE  :'\r'? '\n' -> skip;
WS       : (' '|'\t') -> skip;
MODIFIER  : 'override' | 'static' ;
BOOLEAN  : 'true' | 'false' ;
VOID     : 'void' ;
PRIMITIVE: 'int' | 'boolean' | 'string' ;
INT      : [0-9]+ ;
ID       : [a-z_]+[A-Za-z_0-9]* ;
CLASSNAME: [A-Z]+[A-Za-z_0-9]* ;
COMMENT  : '/*' .*? '*/' -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip ;
STRING   : '"' ~["\\]* '"' { setText(getText().substring(1, getText().length() - 1)); } ;

ERROR    : . ;
