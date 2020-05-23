import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

enum VarType{ INT, REAL, UNKNOWN }

class Value{
    public String name;
    public VarType type;
    public Value( String name, VarType type ){
        this.name = name;
        this.type = type;
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
    HashMap<String, Tab> tabs = new HashMap<>();
    Stack<Value> stack = new Stack<>();

    @Override
    public void exitAssign(MigaParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        variables.put(ID, v.type);
        if( v.type == VarType.INT ){
            LLVMGenerator.assign_i32(ID, v.name);
        }
        if( v.type == VarType.REAL ){
            LLVMGenerator.assign_double(ID, v.name);
        }
    }

    @Override
    public void exitDeclare(MigaParser.DeclareContext ctx) {
        String ID = ctx.ID().getText();
        VarType v = getTypeName(ctx.TYPE_NAME().toString());
        variables.put(ID, v);

        if( v == VarType.INT ){
            LLVMGenerator.declare_i32(ID);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_double(ID);
        }
    }

    @Override
    public void exitDeclare_tab(MigaParser.Declare_tabContext ctx) {
        String ID = ctx.ID().getText();
        String len = ctx.INT().getText();
        VarType v = getTypeName(ctx.TYPE_NAME().getText());
        tabs.put(ID, new Tab(ID, v, len));

        if( v == VarType.INT ){
            LLVMGenerator.declare_tab_i32(ID, len);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_tab_double(ID, len);
        }
    }

    @Override
    public void exitGet_tab_val(MigaParser.Get_tab_valContext ctx) {
        String ID = ctx.ID().getText();
        String element = ctx.INT().getText();
        var tab = tabs.get(ID);

        if( tab.type == VarType.INT ){
            LLVMGenerator.load_tab_i32(ID, element, tab.length);
            stack.push( new Value(""+(LLVMGenerator.reg-1), VarType.INT) );
        }
        if( tab.type == VarType.REAL ){

        }
    }

    @Override
    public void exitSet_tab_val(MigaParser.Set_tab_valContext ctx) {
        Value v = stack.pop();
        var tab = stack.pop();
        if( v.type == VarType.INT ){
            LLVMGenerator.assign_i32(tab.name, v.name);
        }
        if( v.type == VarType.REAL ){
        }

    }

    @Override
    public void enterDeclare_and_assign(MigaParser.Declare_and_assignContext ctx) {
        VarType v = getTypeName(ctx.TYPE_NAME().toString());
        String ID = ctx.assign().ID().getText();

        variables.put(ID, v);

        if( v == VarType.INT ){
            LLVMGenerator.declare_i32(ID);
        }
        if( v == VarType.REAL ){
            LLVMGenerator.declare_double(ID);
        }
    }

    @Override
    public void exitAdd(MigaParser.AddContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.add_i32(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.INT) );
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.REAL) );
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
                LLVMGenerator.sub_i32(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.INT) );
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.sub_double(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.REAL) );
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch");
        }
    }

    @Override
    public void exitMult(MigaParser.MultContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.mult_i32(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.INT) );
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.REAL) );
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch");
        }
    }

    @Override
    public void exitDivide(MigaParser.DivideContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if( v1.type == v2.type ) {
            if( v1.type == VarType.INT ){
                LLVMGenerator.div_i32(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.INT) );
            }
            if( v1.type == VarType.REAL ){
                LLVMGenerator.div_double(v1.name, v2.name);
                stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.REAL) );
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch");
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
    public void exitToint(MigaParser.TointContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.fptosi( v.name );
        stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.INT) );
    }

    @Override
    public void exitToreal(MigaParser.TorealContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.sitofp( v.name );
        stack.push( new Value("%"+(LLVMGenerator.reg-1), VarType.REAL) );
    }

    @Override
    public void exitProg(MigaParser.ProgContext ctx) {
        var ll = LLVMGenerator.generate();

        System.out.println(ll);
        writeToFile("output.ll", ll);
    }

    @Override
    public void exitPrint(MigaParser.PrintContext ctx) {
        String ID = ctx.ID().getText();
        VarType type = variables.get(ID);
        if( type != null ) {
            if( type == VarType.INT ){
                LLVMGenerator.printf_i32( ID );
            }
            if( type == VarType.REAL ){
                LLVMGenerator.printf_double( ID );
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable "+ID);
        }
    }

    @Override
    public void exitPrint_tab(MigaParser.Print_tabContext ctx) {
        var tab_element = stack.pop();
        if(tab_element.type != null ) {
            if( tab_element.type == VarType.INT ){
                LLVMGenerator.printf_i32( tab_element.name );
            }
            if( tab_element.type == VarType.REAL ){
                LLVMGenerator.printf_double( tab_element.name );
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable "+tab_element.name);
        }
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

    private VarType getTypeName(String type) {
        VarType ans;
        switch (type) {
            case "int": ans = VarType.INT;
            break;
            case "float": ans = VarType.REAL;
            break;
            default: ans = VarType.UNKNOWN;
            break;
        }
        return ans;
    }
}
