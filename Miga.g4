grammar Miga;

prog: ( (stat|function)? NEWLINE )*
;

block: ( stat? NEWLINE )*
;

stat: declare
    | assign
    | declare_and_assign
    | tab_lang
	| system_func
	| loop
	| if_statement
	| func_call
	| struct_lang
;
if_block: block
;

if_statement: IF conditional BEGIN if_block END
;

fblock: block
;

func_call: ID LEFT_ROUND_BRACKET RIGHT_ROUND_BRACKET
;

function: FUNCTION TYPE_NAME ID BEGIN fblock END
;

fparam: LEFT_ROUND_BRACKET ID RIGHT_ROUND_BRACKET
;

loop_cond: conditional
;

loop_block: block
;

loop: LOOP loop_cond BEGIN loop_block END
;


conditional: expr EQUAL_SIGN expr #equal
    | expr NOT_EQUAL_SIGN expr #notequal
    | expr LESS_SIGN expr #less
    | expr LESS_EQUAL_SIGN expr #lessequal
    | expr MORE_SIGN expr #more
    | expr MORE_EQUAL_SIGN expr #moreequal
;

declare: TYPE_NAME ID
;

assign: ID ASSIGN expr
;

declare_and_assign: TYPE_NAME assign
;

struct_lang: declare_struct
    | get_struct_val
    | set_struct_val
    | print_struct_val
    | create_struct
;

struct_block: ( (TYPE_NAME ID)? NEWLINE )*
;

create_struct: STRUCT ID BEGIN struct_block END
;

declare_struct: STRUCT ID ID
;

get_struct_val: ID '.' ID
;

set_struct_val: get_struct_val ASSIGN expr
;

print_struct_val: PRINT get_struct_val
;

tab_lang: declare_tab
    | get_tab_val
    | set_tab_val
    | print_tab
;

declare_tab: TYPE_NAME LEFT_BRACKET INT RIGHT_BRACKET ID
;

get_tab_val: ID LEFT_BRACKET expr RIGHT_BRACKET
;

set_tab_val: get_tab_val ASSIGN expr
;

print_tab: PRINT get_tab_val
;

system_func: print
    | read
    | return_stat
;

print: PRINT ID
    | PRINT STRING
;

read: READ ID
;

return_stat: RETURN expr
;

expr:  arithemtic_expr
;

arithemtic_expr:  element   #single_value
    | element ADD element   #add
    | element SUBTRACT element  #subtract
    | element MULT element  #mult
    | element DIVIDE element    #divide
    | '(' arithemtic_expr ')'  #par
;

element:   INT          #int
    | REAL              #real
    | TOINT element     #toint
    | TOREAL element    #toreal
    | ID                #id
    | get_tab_val #gettabval
    | func_call #funccal
    | get_struct_val #getstructval
;

STRUCT: 'struct'
;

TYPE_NAME: 'int'
    | 'float'
;

RETURN: 'return'
;

PRINT:	'print'
;

READ:	'read'
;

FUNCTION: 'func'
;

LOOP: 'while'
;

IF:	'if'
;

BEGIN: '{'
;

END: '}'
;

LEFT_ROUND_BRACKET: '('
;

RIGHT_ROUND_BRACKET: ')'
;

LEFT_BRACKET: '['
;

RIGHT_BRACKET: ']'
;

TOINT: '(int)'
;

TOREAL: '(real)'
;

ID:   ('a'..'z'|'A'..'Z')+
;

REAL: '0'..'9'+'.''0'..'9'+
;

INT: '0'..'9'+
;

STRING: '"' ( ~('\\'|'"') )* '"'
;

ADD: '+'
;

SUBTRACT: '-'
;

MULT: '*'
;

DIVIDE: '/'
;

ASSIGN: '='
;

EQUAL_SIGN: '=='
;

NOT: '!'
;

NOT_EQUAL_SIGN: '!='
;

LESS_SIGN: '<'
;

LESS_EQUAL_SIGN: '<='
;

MORE_SIGN: '>'
;

MORE_EQUAL_SIGN: '>='
;

NEWLINE:	'\r'? '\n'
;

WS:   (' '|'\t')+ { skip(); }
;
