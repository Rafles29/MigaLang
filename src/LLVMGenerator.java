import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Stack;

class LLVMGenerator{
   
   static String header_text = "";
   static String main_text = "";
   static String buffer = "";
   static int reg = 1;
   static int main_reg = 1;
   static int br = 0;

   static Stack<Integer> brstack = new Stack<>();

   static void functionstart_i32(String id){
      main_text += buffer;
      main_reg = reg;
      buffer = "define i32 @"+id+"() nounwind {\n";
      reg = 1;
   }

   static void functionstart_double(String id){
      main_text += buffer;
      main_reg = reg;
      buffer = "define double @"+id+"() nounwind {\n";
      reg = 1;
   }

   static void functionend(){
      buffer += "}\n";
      header_text += buffer;
      buffer = "";
      reg = main_reg;
   }

   static  void return_i32(String val) {
      buffer += "ret i32 "+val+"\n";
   }

   static  void return_double(String val) {
      buffer += "ret double "+val+"\n";
   }

   static void call_i32(String id){
      buffer += "%"+reg+" = call i32 @"+id+"()\n";
      reg++;
   }

   static void call_double(String id){
      buffer += "%"+reg+" = call double @"+id+"()\n";
      reg++;
   }

   static void close_main(){
      main_text += buffer;
   }
   
   static void ifstart(){
      br++;
      buffer += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      buffer += "true"+br+":\n";
      brstack.push(br);
   }

   static void ifend(){
      int b = brstack.pop();
      buffer += "br label %false"+b+"\n";
      buffer += "false"+b+":\n";
   }

   static void while_start() {
      br++;
      buffer += "br label %cond"+br+"\n";
      buffer += "cond"+br+":\n";
   }

   static void while_cond_end() {
      buffer += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      buffer += "true"+br+":\n";
      brstack.push(br);
   }

   static void while_end(){
      int b = brstack.pop();
      buffer += "br label %cond"+b+"\n";
      buffer += "false"+b+":\n";
   }

   static void equal_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp eq i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void notequal_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp ne i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp slt i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp sgt i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_equal_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp sle i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_equal_i32(String val1, String val2){
      buffer += "%"+reg+" = icmp sge i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void equal_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp oeq double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void notequal_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp une double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp olt double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp ogt double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_equal_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp ole double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_equal_double(String val1, String val2){
      buffer += "%"+reg+" = fcmp oge double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void printf_i32(String id){
      load_i32(id);
      buffer += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %"+(reg-1)+")\n";
      reg++;
   }

   static void printf_double(String id){
      load_double(id);
      buffer += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %"+(reg-1)+")\n";
      reg++;
   }

   static void scanf_i32(String id){
      buffer += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strsi, i32 0, i32 0), i32* "+id+")\n";
      reg++;
   }

   static void scanf_double(String id){
      buffer += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strsd, i32 0, i32 0), double* "+id+")\n";
      reg++;
   }

   static void declare_i32(String id, Boolean global){
      if( global ){
         header_text += "@"+id+" = global i32 0\n";
      } else {
         buffer += "%"+id+" = alloca i32\n";
      }
   }

   static void declare_double(String id, Boolean global){
      if( global ){
         header_text += "@"+id+" = global double 0.0\n";
      } else {
         buffer += "%"+id+" = alloca double\n";
      }
   }

   static void assign_i32(String id, String value) {
      buffer += "store i32 "+value+", i32* "+id+"\n";
   }

   static void assign_double(String id, String value) {
      buffer += "store double "+value+", double* "+id+"\n";
   }


   static void load_i32(String id){
      buffer += "%"+reg+" = load i32, i32* "+id+"\n";
      reg++;
   }

   static void load_double(String id){
      buffer += "%"+reg+" = load double, double* "+id+"\n";
      reg++;
   }

   static void add_i32(String val1, String val2){
      buffer += "%"+reg+" = add i32 "+val1+", "+val2+"\n";
      reg++;
   }

   static void sub_i32(String val1, String val2){
      buffer += "%"+reg+" = sub i32 "+val2+", "+val1+"\n";
      reg++;
   }

   static void add_double(String val1, String val2){
      buffer += "%"+reg+" = fadd double "+val1+", "+val2+"\n";
      reg++;
   }

   static void sub_double(String val1, String val2){
      buffer += "%"+reg+" = fsub double "+val2+", "+val1+"\n";
      reg++;
   }

   static void mult_i32(String val1, String val2){
      buffer += "%"+reg+" = mul i32 "+val1+", "+val2+"\n";
      reg++;
   }

   static void mult_double(String val1, String val2){
      buffer += "%"+reg+" = fmul double "+val1+", "+val2+"\n";
      reg++;
   }

   static void div_i32(String val1, String val2){
      buffer += "%"+reg+" = div i32 "+val2+", "+val1+"\n";
      reg++;
   }

   static void div_double(String val1, String val2){
      buffer += "%"+reg+" = fdiv double "+val2+", "+val1+"\n";
      reg++;
   }

   static void declare_tab_i32(String id, String nmbOfElements, boolean global) {
      if (global) {
         header_text += "@"+id+" = common global ["+nmbOfElements+" x i32] zeroinitializer\n";
      } else {
         buffer += "%"+id+" =  alloca ["+nmbOfElements+" x i32]\n";
      }
   }

   static void declare_tab_double(String id, String nmbOfElements, boolean global) {
      if (global) {
         header_text += "@"+id+" = common global ["+nmbOfElements+" x double] zeroinitializer\n";
      } else {
         buffer += "%"+id+" =  alloca ["+nmbOfElements+" x double]\n";
      }
   }

   static void fptosi(String id){
      buffer += "%"+reg+" = fptosi double "+id+" to i32\n";
      reg++;
   }

   static void sitofp(String id){
      buffer += "%"+reg+" = sitofp i32 "+id+" to double\n";
      reg++;
   }

   static void load_tab_i32(String id, String element, String len){
      buffer += "%"+reg+" = getelementptr inbounds ["+len+" x i32], ["+len+" x i32]* "+id+", i32 0, i32 "+element+"\n";
      reg++;
   }

   static void load_tab_double(String id, String element, String len){
      buffer += "%"+reg+" = getelementptr inbounds ["+len+" x double], ["+len+" x double]* "+id+", i32 0, i32 "+element+"\n";
      reg++;
   }

   static void print_String(String output) {
      var address = "@.str."+reg;
      header_text += address+" = private unnamed_addr constant ["+output.length()+" x i8] c"+output.substring(0,output.length()-1)+"\\0A\\00\"\n";
      buffer += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (["+output.length()+" x i8], ["+output.length()+" x i8]* "+address+", i32 0, i32 0))\n";
      reg++;
   }

   static void create_struct(Struct newStruct) {
      header_text += "%struct."+newStruct.name+" = type { ";
      newStruct.types.forEach(value -> {
         if (value.type == VarType.INT) {
            header_text += "i32, ";
         }
         else
            header_text += "double, ";
      });
      header_text =  header_text.substring(0, header_text.length()-2);
      header_text += " }\n";
   }

   static void declare_struct(String structName, String id, boolean global) {
      if( global ){
         header_text += "@"+id+" = common global %struct."+structName+" zeroinitializer\n";
      } else {
         buffer += "%"+id+" = alloca %struct."+structName+"\n";
      }
   }

   static void load_struct(String structName, String id, String element){
      buffer += "%"+reg+" = getelementptr inbounds %struct."+structName+", %struct."+structName+"* "+id+", i32 0, i32 "+element+"\n";
      reg++;
   }

   static String generate(){
      String text = "";
      text += "declare i32 @printf(i8*, ...)\n";
      text += "declare i32 @scanf(i8*, ...)\n";
      text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
      text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
      text += "@strsi = constant [3 x i8] c\"%d\\00\"\n";
      text += "@strsd = constant [4 x i8] c\"%lf\\00\"\n";
      text += header_text;
      text += "define i32 @main() {\n";
      text += main_text;
      text += "ret i32 0 }\n";
      return text;
   }

}
