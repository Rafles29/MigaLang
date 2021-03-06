import org.antlr.v4.runtime.ParserRuleContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

enum VarType{ INT, REAL, UNKNOWN }

class Value{
    public String name;
    public VarType type;
    public Value( String name, VarType type ){
        this.name = name;
        this.type = type;
    }
}

class Struct {
    public String name;
    public ArrayList<Value> types = new ArrayList<Value>();

    public Struct(String name) {
        this.name = name;
    }
}

class Tab {
    public String name;
    public VarType type;
    public String length;

    public Tab(String name, VarType type, String length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }
}

public class MigaActions extends MigaBaseListener {
    HashMap<String, VarType> variables = new HashMap<>();
    HashMap<String, VarType> globalVariables = new HashMap<>();
    HashMap<String, Struct> structs = new HashMap<>();
    HashMap<String, String> structVariables = new HashMap<>();
    HashMap<String, Tab> tabs = new HashMap<>();
    HashMap<String, VarType> functions = new HashMap<>();
    Stack<Value> stack = new Stack<>();
    Boolean global = true;
    Struct tmpStruct;

    @Override
    public void enterFunction(MigaParser.FunctionContext ctx) {
        global = false;
        var ID = ctx.ID().getText();
        var funcType = getTypeName(ctx.TYPE_NAME().toString(), ctx);
        functions.put(ID, funcType);
        if( funcType == VarType.INT ){
            LLVMGenerator.functionstart_i32(ID);
        }
        if( funcType == VarType.REAL ){
            LLVMGenerator.functionstart_double(ID);
        }
    }

    @Override
    public void exitFunction(MigaParser.FunctionContext ctx) {
        global = true;
        variables = new HashMap<>();
        LLVMGenerator.functionend();
    }

    @Override
    public void exitFunc_call(MigaParser.Func_callContext ctx) {
        var ID = ctx.ID().getText();
        var funcType = functions.get(ID);
        if( funcType == VarType.INT ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.call_i32(ID);
        }
        if( funcType == VarType.REAL ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.call_double(ID);
        }
    }

    @Override
    public void exitAssign(MigaParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        putVariable(ID, v.type);
        if( v.type == VarType.INT ){
            LLVMGenerator.assign_i32(addPrefix(ID), v.name);
        }
        if( v.type == VarType.REAL ){
            LLVMGenerator.assign_double(addPrefix(ID), v.name);
        }
    }

    @Override
    public void exitDeclare(MigaParser.DeclareContext ctx) {
        String ID = ctx.ID().getText();
        VarType v = getTypeName(ctx.TYPE_NAME().toString(), ctx);
        putVariable(ID, v);

        if( v == VarType.INT ){
            LLVMGenerator.declare_i32(ID, global);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_double(ID, global);
        }
    }

    @Override
    public void exitDeclare_tab(MigaParser.Declare_tabContext ctx) {
        String ID = ctx.ID().getText();
        String len = ctx.INT().getText();
        VarType v = getTypeName(ctx.TYPE_NAME().getText(), ctx);
        tabs.put(ID, new Tab(ID, v, len));
        putVariable(ID, v);
        if( v == VarType.INT ){
            LLVMGenerator.declare_tab_i32(ID, len, global);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_tab_double(ID, len, global);
        }
    }

    @Override
    public void exitGet_tab_val(MigaParser.Get_tab_valContext ctx) {
        String ID = ctx.ID().getText();
        var element = stack.pop();
        var tab = tabs.get(ID);

        if( tab.type == VarType.INT ){
            stack.push( new Value(""+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.load_tab_i32(addPrefix(tab.name), element.name, tab.length);
        }
        if( tab.type == VarType.REAL ){
            stack.push( new Value(""+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.load_tab_double(addPrefix(tab.name), element.name, tab.length);
        }
    }

    @Override
    public void exitGet_struct_val(MigaParser.Get_struct_valContext ctx) {
        var id = ctx.ID(0).getText();
        var field = ctx.ID(1).getText();
        var structName = structVariables.get(id);

        var struct = structs.get(structName);
        struct.types.get(0);

        int index = 0;
        for (int i = 0; i< struct.types.size(); i++) {
            if (struct.types.get(i).name.equals(field)) {
                index = i;
            }
        }
        var structField = struct.types.get(index);

        if( structField.type == VarType.INT ){
            stack.push( new Value(""+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.load_struct(structName, addPrefix(id), Integer.toString(struct.types.indexOf(structField)));
        }
        if( structField.type == VarType.REAL ){
            stack.push( new Value(""+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.load_struct(structName, addPrefix(id), Integer.toString(struct.types.indexOf(structField)));
        }
    }

    @Override
    public void exitSet_struct_val(MigaParser.Set_struct_valContext ctx) {
        Value v = stack.pop();
        var struct = stack.pop();
        if( v.type == VarType.INT ) {
            LLVMGenerator.assign_i32(addPrefix(struct.name), v.name);
        }
        if( v.type == VarType.REAL ) {
            LLVMGenerator.assign_double(addPrefix(struct.name), v.name);
        }
    }

    @Override
    public void exitSet_tab_val(MigaParser.Set_tab_valContext ctx) {
        Value v = stack.pop();
        var tab = stack.pop();
        if( v.type == VarType.INT ) {
            LLVMGenerator.assign_i32(addPrefix(tab.name), v.name);
        }
        if( v.type == VarType.REAL ) {
            LLVMGenerator.assign_double(addPrefix(tab.name), v.name);
        }

    }

    @Override
    public void enterDeclare_and_assign(MigaParser.Declare_and_assignContext ctx) {
        VarType v = getTypeName(ctx.TYPE_NAME().toString(), ctx);
        String ID = ctx.assign().ID().getText();

        if( v == VarType.INT ){
            LLVMGenerator.declare_i32(ID, global);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_double(ID, global);
        }
    }

    @Override
    public void exitAdd(MigaParser.AddContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
                LLVMGenerator.add_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
                LLVMGenerator.add_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch");
        }
    }

    @Override
    public void exitSubtract(MigaParser.SubtractContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
                LLVMGenerator.sub_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
                LLVMGenerator.sub_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "subtract type mismatch");
        }
    }

    @Override
    public void exitMult(MigaParser.MultContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
                LLVMGenerator.mult_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
                LLVMGenerator.mult_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "multiply type mismatch");
        }
    }

    @Override
    public void exitDivide(MigaParser.DivideContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
                LLVMGenerator.div_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
                LLVMGenerator.div_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }
    }

    @Override
    public void exitInt(MigaParser.IntContext ctx) {
        stack.push( new Value(ctx.INT().getText(), VarType.INT) );
    }

    @Override
    public void exitReal(MigaParser.RealContext ctx) {
        stack.push( new Value(ctx.REAL().getText(), VarType.REAL) );
    }

    @Override
    public void exitId(MigaParser.IdContext ctx) {
        var ID = ctx.ID().getText();
        var type = getVariable(ID);
        if( type == VarType.INT ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.load_i32(addPrefix(ID));
        }
        if( type == VarType.REAL ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.load_double(addPrefix(ID));
        }
    }

    @Override
    public void exitGettabval(MigaParser.GettabvalContext ctx) {
        var tab = stack.pop();
        if( tab.type == VarType.INT ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.load_i32(addPrefix(tab.name));
        }
        if( tab.type == VarType.REAL ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.load_double(addPrefix(tab.name));
        }
    }

    @Override
    public void exitGetstructval(MigaParser.GetstructvalContext ctx) {
        var tab = stack.pop();
        if( tab.type == VarType.INT ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
            LLVMGenerator.load_i32(addPrefix(tab.name));
        }
        if( tab.type == VarType.REAL ){
            stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
            LLVMGenerator.load_double(addPrefix(tab.name));
        }
    }

    @Override
    public void exitFunccal(MigaParser.FunccalContext ctx) {
        super.exitFunccal(ctx);
    }

    @Override
    public void exitToint(MigaParser.TointContext ctx) {
        Value v = stack.pop();
        stack.push( new Value("%"+(LLVMGenerator.reg), VarType.INT) );
        LLVMGenerator.fptosi( v.name );
    }

    @Override
    public void exitToreal(MigaParser.TorealContext ctx) {
        Value v = stack.pop();
        stack.push( new Value("%"+(LLVMGenerator.reg), VarType.REAL) );
        LLVMGenerator.sitofp( v.name );
    }

    @Override
    public void exitProg(MigaParser.ProgContext ctx) {
        LLVMGenerator.close_main();
        var ll = LLVMGenerator.generate();

        System.out.println(ll);
        writeToFile("output.ll", ll);
    }

    @Override
    public void exitReturn_stat(MigaParser.Return_statContext ctx) {
        var v = stack.pop();

        if( v.type == VarType.INT ){
            LLVMGenerator.return_i32(v.name);
        }
        if( v.type == VarType.REAL ){
            LLVMGenerator.return_double(v.name);
        }
    }

    @Override
    public void enterIf_block(MigaParser.If_blockContext ctx) {
        LLVMGenerator.ifstart();
    }

    @Override
    public void exitIf_block(MigaParser.If_blockContext ctx) {
        LLVMGenerator.ifend();
    }

    @Override
    public void enterLoop(MigaParser.LoopContext ctx) {
        LLVMGenerator.while_start();
    }

    @Override
    public void exitLoop_block(MigaParser.Loop_blockContext ctx) {
        LLVMGenerator.while_end();
    }

    @Override
    public void exitLoop_cond(MigaParser.Loop_condContext ctx) {
        LLVMGenerator.while_cond_end();
    }

    @Override
    public void exitEqual(MigaParser.EqualContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.equal_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.equal_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitNotequal(MigaParser.NotequalContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.notequal_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.notequal_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitLess(MigaParser.LessContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.less_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.less_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitLessequal(MigaParser.LessequalContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.less_equal_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.less_equal_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitMore(MigaParser.MoreContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.more_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.more_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitMoreequal(MigaParser.MoreequalContext ctx) {
        var v1 = stack.pop();
        var v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.more_equal_i32(v1.name, v2.name);
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.more_equal_double(v1.name, v2.name);
            }
        } else {
            error(ctx.getStart().getLine(), "type mismatch");
        }
    }

    @Override
    public void exitPrint(MigaParser.PrintContext ctx) {
        if (ctx.STRING() != null) {
            LLVMGenerator.print_String(ctx.STRING().getText());
        } else {
            String ID = ctx.ID().getText();
            VarType type = getVariable(ID);
            if (type != null) {
                if (type == VarType.INT) {
                    LLVMGenerator.printf_i32(addPrefix(ID));
                }
                if (type == VarType.REAL) {
                    LLVMGenerator.printf_double(addPrefix(ID));
                }
            } else {
                error(ctx.getStart().getLine(), "unknown variable " + ID);
            }
        }
    }

    @Override
    public void exitPrint_tab(MigaParser.Print_tabContext ctx) {
        var tab_element = stack.pop();
        if(tab_element.type != null ) {
            if( tab_element.type == VarType.INT ){
                LLVMGenerator.printf_i32(addPrefix(tab_element.name) );
            }
            if( tab_element.type == VarType.REAL ){
                LLVMGenerator.printf_double(addPrefix(tab_element.name) );
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable "+tab_element.name);
        }
    }

    @Override
    public void exitPrint_struct_val(MigaParser.Print_struct_valContext ctx) {
        var tab_element = stack.pop();
        if(tab_element.type != null ) {
            if( tab_element.type == VarType.INT ){
                LLVMGenerator.printf_i32(addPrefix(tab_element.name) );
            }
            if( tab_element.type == VarType.REAL ){
                LLVMGenerator.printf_double(addPrefix(tab_element.name) );
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable "+tab_element.name);
        }
    }

    @Override
    public void exitRead(MigaParser.ReadContext ctx) {
        String ID = ctx.ID().getText();
        var v = getVariable(ID);
        if( v == VarType.INT ) {
            LLVMGenerator.scanf_i32(addPrefix(ID));
        }
        if( v == VarType.REAL ) {
            LLVMGenerator.scanf_double(addPrefix(ID));
        }
    }

    @Override
    public void enterCreate_struct(MigaParser.Create_structContext ctx) {
        var name = ctx.ID().getText();
        tmpStruct = new Struct(name);
        structs.put(name, tmpStruct);
    }

    @Override
    public void exitStruct_block(MigaParser.Struct_blockContext ctx) {
        for (int i = 0; i < ctx.ID().size(); i++)
            tmpStruct.types.add(new Value(ctx.ID(i).getText(), getTypeName(ctx.TYPE_NAME(i).getText(), ctx)));
    }

    @Override
    public void exitCreate_struct(MigaParser.Create_structContext ctx) {
        LLVMGenerator.create_struct(tmpStruct);
    }

    @Override
    public void exitDeclare_struct(MigaParser.Declare_structContext ctx) {
        var structName = ctx.ID(0).getText();
        var varName = ctx.ID(1).getText();

        putVariable(varName, VarType.UNKNOWN);
        structVariables.put(varName, structName);
        LLVMGenerator.declare_struct(structName, varName, global);
    }

    private void putVariable(String name, VarType type) {
        if (global) {
            globalVariables.put(name, type);
        } else {
            variables.put(name, type);
        }
    }

    private VarType getVariable(String name) {
        if (isVarGlobal(name)) {
            return globalVariables.get(name);
        } else {
            return variables.get(name);
        }
    }

    private boolean isVarGlobal(String name) {
        return globalVariables.get(name) != null;
    }

    private String addPrefix(String name) {
        return  isVarGlobal(name) ? "@" + name: "%" + name;
    }

    private void error(int line, String msg){
        System.err.println("Error, line "+line+", "+msg);
        System.exit(1);
    }

    private void writeToFile(String fileName, String data) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VarType getTypeName(String type, ParserRuleContext ctx) {
        VarType ans;
        switch (type) {
            case "int" -> ans = VarType.INT;
            case "float" -> ans = VarType.REAL;
            default -> {
                ans = VarType.UNKNOWN;
                error(ctx.getStart().getLine(), "unknown type");
            }
        }
        return ans;
    }
}
