import com.sun.javadoc.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.ArrayList;
//import java.lang.reflect.*;


public class Parser{
	
	
	public static void main(String args[]) {
        try {
			String source_Path = args[0] + "\\*";
			String output = args[1] + ".txt";
			Process javaDc = Runtime.getRuntime().exec("javadoc -doclet Parser -docletpath . -private " + source_Path + " -sourcepath " + args[1] + " -subpackages " + args[0]);
			javaDc.waitFor();
			
			Process uml = Runtime.getRuntime().exec("java -jar plantuml.jar " + output);
			uml.waitFor();
		}// try in main
		catch(Exception e){}
	
	} // main()
	
	
    public static boolean start(RootDoc rootdoc) {
		try {
			ClassDoc[] classes_root = rootdoc.classes();
			
			// Get name of output file from options.
			String[][] option_root = rootdoc.options();
			String uml_Grammer = option_root[5][1] + ".txt";
			String Path_code = option_root[6][1] + "\\";
			
			// Get package name.
			PackageDoc packages_names = classes_root[0].containingPackage();
			
			File inputFile = new File(uml_Grammer);
			FileOutputStream outputFileS = new FileOutputStream(inputFile);
			PrintWriter printWrite = new PrintWriter(outputFileS);
			
			printWrite.println("@startuml");
			printWrite.println("skinparam classAttributeIconSize 0");
			if (!(packages_names.name().equals("")))
				printWrite.println("package " + packages_names.name() + " <<Folder>> {");
						
			ArrayList<String> interfaceClass_list = new ArrayList<String>();
			ArrayList<String> class_List = new ArrayList<String>();
			ArrayList<String> allClass_List = new ArrayList<String>();
			ArrayList<String> classPairsTo_Exclude = new ArrayList<String>();
			ArrayList<String> interfacePairsTo_Exclude = new ArrayList<String>();

			for (int i = 0; i < classes_root.length; ++i) {
				allClass_List.add(classes_root[i].name());
				if (classes_root[i].isInterface())
					interfaceClass_list.add(classes_root[i].name());
				
				else
					class_List.add(classes_root[i].name());
				
			}
						
			for (int i = 0; i < classes_root.length; ++i) {
				// check for Interface .
				boolean is_Interface = classes_root[i].isInterface();
			
				// Get  Attributes of Class
				FieldDoc[] attr = classes_root[i].fields();
				for (int k=0; k < attr.length; k++) {
					
					if(attr[k].isPublic() || attr[k].isPrivate()){
						if (interfaceClass_list.contains(attr[k].type().typeName())) {
							String tmp = classes_root[i].name() + ":" + attr[k].type().typeName();
							if (!(interfacePairsTo_Exclude.contains(tmp))) {
								printWrite.println(classes_root[i].name() + " ..> " + attr[k].type().typeName() + " : uses");
								interfacePairsTo_Exclude.add(tmp);
							}	
						}	
						else if(!(attr[k].type().typeName().toString().equals("String"))) {
							// Check conditions for both interface and class.
							if (class_List.contains(attr[k].type().typeName())) {
								String tmp_class = classes_root[i].name() + ":" + attr[k].type().typeName();
								String tmp_class_r = attr[k].type().typeName() + ":" + classes_root[i].name();
								
								if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
									
									if ((attr[k].type().dimension().toString()).equals(""))
										printWrite.print(attr[k].type().typeName() + " \"1\" " + " --");
									else
									printWrite.print(attr[k].type().typeName() + " \"*\" " + " --");
									boolean is_Second_Class_Set = false;
									for (int i1 = 0; i1 < classes_root.length; ++i1) {
										if (classes_root[i1].name().equals(attr[k].type().typeName())) {
											FieldDoc[] attr1 = classes_root[i1].fields();
											for (int k1=0; k1 < attr1.length; k1++) {
												if(attr1[k1].isPublic() || attr1[k1].isPrivate()){
													if ((attr1[k1].type().typeName()).equals(classes_root[i].name())) {
														is_Second_Class_Set = true;
														if ((attr1[k1].type().dimension().toString()).equals(""))
															printWrite.println(" \"1\" " + classes_root[i].name());
														else
															printWrite.println(" \"*\" " + classes_root[i].name());
													}
												}
											}
										}
									}
									if (!is_Second_Class_Set) {
										printWrite.println(" " + classes_root[i].name());
									}
										
									classPairsTo_Exclude.add(tmp_class);
									classPairsTo_Exclude.add(tmp_class_r);
								}
							}
							else if(!(attr[k].type().isPrimitive())) {
								String javaFPath = Path_code + classes_root[i].name() + ".java";
								String generic_String = get_Generic_Type(javaFPath, attr[k].name());
								if (interfaceClass_list.contains(generic_String)) {
									String tmp = classes_root[i].name() + ":" + generic_String;
									if (!(interfacePairsTo_Exclude.contains(tmp))) {
										printWrite.println(classes_root[i].name() + " ..> " + generic_String + " : uses");
										interfacePairsTo_Exclude.add(tmp);
									}
								}	
								else if (class_List.contains(generic_String)) {
								String tmp_class = classes_root[i].name() + ":" + generic_String;
								String tmp_class_r = generic_String + ":" + classes_root[i].name();
								
								if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
									
									printWrite.print(generic_String + " \"*\" " + " --");
									boolean is_Second_Class_Set = false;
									for (int i1 = 0; i1 < classes_root.length; ++i1) {
										if (classes_root[i1].name().equals(generic_String)) {
											FieldDoc[] attr1 = classes_root[i1].fields();
											for (int k1=0; k1 < attr1.length; k1++) {
												if(attr1[k1].isPublic() || attr1[k1].isPrivate()){
													if ((attr1[k1].type().typeName()).equals(classes_root[i].name())) {
														is_Second_Class_Set = true;
														if ((attr1[k1].type().dimension().toString()).equals("")) {
															printWrite.println(" \"1\" " + classes_root[i].name());
														}
														else {
															printWrite.println(" \"*\" " + classes_root[i].name());
														}
													}
												}
											}
										}
									}
									if (!is_Second_Class_Set) {
										printWrite.println(" " + classes_root[i].name());
									}
										
									classPairsTo_Exclude.add(tmp_class);
									classPairsTo_Exclude.add(tmp_class_r);
								}
								}	
							}
						}
					}	
				}//for loop for the attr iteration
				
				// Gets back Method names, return type and arguments.
				MethodDoc[] methods = classes_root[i].methods();
				
				for (int j=0; j < methods.length; j++) {
					Parameter[] params = methods[j].parameters();
					for (int l=0; l < params.length; l++) {
						if (interfaceClass_list.contains(params[l].type().typeName())) {
							String tmp = classes_root[i].name() + ":" + params[l].type().typeName();
							if (!(interfacePairsTo_Exclude.contains(tmp))) {
								printWrite.println(classes_root[i].name() + " ..> " + params[l].type().typeName() + " : uses");
								interfacePairsTo_Exclude.add(tmp);
							}		
						}
						else if(!(params[l].type().typeName().toString().equals("String"))) {
							// Add conditions which is for both interface and class.
							if (class_List.contains(params[l].type().typeName())) {
								
								String tmp_class = classes_root[i].name() + ":" + params[l].type().typeName();
								String tmp_class_r = params[l].type().typeName() + ":" + classes_root[i].name();
								
								if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
									if ((params[l].type().dimension().toString()).equals(""))
										printWrite.println(classes_root[i].name() + " -- " + params[l].type().typeName());
									else
										printWrite.println(classes_root[i].name() + "" + " --" + " \"*\" " + params[l].type().typeName());
									classPairsTo_Exclude.add(tmp_class);
									classPairsTo_Exclude.add(tmp_class_r);
								}
							}
							else if(!(params[l].type().isPrimitive())) {
								String javaFPath = Path_code + classes_root[i].name() + ".java";
								String generic_String = get_Generic_Type(javaFPath, params[l].name());
								if (interfaceClass_list.contains(generic_String)) {
									
									String tmp = classes_root[i].name() + ":" + generic_String;
									if (!(interfacePairsTo_Exclude.contains(tmp))) {
										printWrite.println(classes_root[i].name() + " ..> " + generic_String + " : uses");
										interfacePairsTo_Exclude.add(tmp);
									}
									
									
								}	
								else if (class_List.contains(generic_String)) {
									String tmp_class = classes_root[i].name() + ":" + generic_String;
									String tmp_class_r = generic_String + ":" + classes_root[i].name();
								
									if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
										printWrite.println(classes_root[i].name() + "" + " --" + " \"*\" " + generic_String);
										classPairsTo_Exclude.add(tmp_class);
										classPairsTo_Exclude.add(tmp_class_r);
									}	
								}
							}
						}
					}
					
					String javaFPath = Path_code + classes_root[i].name() + ".java";
					String returnVal = checkInClass(javaFPath, methods[j].name(), allClass_List);
					if (!(returnVal.equals("")))
						if (interfaceClass_list.contains(returnVal)) {
							String tmp = classes_root[i].name() + ":" + returnVal;
							if (!(interfacePairsTo_Exclude.contains(tmp))) {
								printWrite.println(classes_root[i].name() + " ..> " + returnVal + " : uses");
								interfacePairsTo_Exclude.add(tmp);
							}
						}						
						else if (class_List.contains(returnVal))
							printWrite.println(classes_root[i].name() + " -- " + returnVal);
				}
				
				ConstructorDoc[] constr = classes_root[i].constructors();
				for (int z=0; z < constr.length; z++){
					Parameter[] params = constr[z].parameters();
				
					for (int l=0; l < params.length; l++) {
						if (interfaceClass_list.contains(params[l].type().typeName())) {
							String tmp = classes_root[i].name() + ":" + params[l].type().typeName();
							if (!(interfacePairsTo_Exclude.contains(tmp))) {
								printWrite.println(classes_root[i].name() + " ..> " + params[l].type().typeName() + " : uses");
								interfacePairsTo_Exclude.add(tmp);
							}
						}
						//////////
						
						else if(!(params[l].type().typeName().toString().equals("String"))) {
							// Add conditions which is for both interface and class.
							if (class_List.contains(params[l].type().typeName())) {
								
								String tmp_class = classes_root[i].name() + ":" + params[l].type().typeName();
								String tmp_class_r = params[l].type().typeName() + ":" + classes_root[i].name();
								
								if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
									if ((params[l].type().dimension().toString()).equals(""))
										printWrite.println(classes_root[i].name() + " -- " + " \"1\" " + params[l].type().typeName());
									else
										printWrite.println(classes_root[i].name() + "" + " --" + " \"*\" " + params[l].type().typeName());
									classPairsTo_Exclude.add(tmp_class);
									classPairsTo_Exclude.add(tmp_class_r);
								}
							}
							else if(!(params[l].type().isPrimitive())) {
								String javaFPath = Path_code + classes_root[i].name() + ".java";
								String generic_String = get_Generic_Type(javaFPath, params[l].name());
								if (interfaceClass_list.contains(generic_String)) {
									
									String tmp = classes_root[i].name() + ":" + generic_String;
									if (!(interfacePairsTo_Exclude.contains(tmp))) {
										printWrite.println(classes_root[i].name() + " ..> " + generic_String + " : uses");
										interfacePairsTo_Exclude.add(tmp);
									}
									
								}	
								else if (class_List.contains(generic_String)) {
									String tmp_class = classes_root[i].name() + ":" + generic_String;
									String tmp_class_r = generic_String + ":" + classes_root[i].name();
								
									if (!(classPairsTo_Exclude.contains(tmp_class)) || !(classPairsTo_Exclude.contains(tmp_class_r))) {
										printWrite.println(classes_root[i].name() + "" + " --" + " \"*\" " + generic_String);
										classPairsTo_Exclude.add(tmp_class);
										classPairsTo_Exclude.add(tmp_class_r);
									}	
								}
							}
						}
					}
				}
							
				// check for Inheritance
				Type classType = classes_root[i].superclassType();
				if (classType != null && !((classType.asClassDoc().name()).equals("Object"))) {
					printWrite.println(classType.asClassDoc().name() + " <|-- " + classes_root[i].name());
				}
				
				if (!is_Interface) {
					/* Get the list of Interfaces which is implemented by this class. */
					Type[] classTypes = classes_root[i].interfaceTypes();
					if (classTypes.length != 0) {
						for (int m=0; m < classTypes.length; m++) {
							printWrite.println(classTypes[m].asClassDoc().name() + " <|.. " + classes_root[i].name());
						}
					}
				}
				
				if (is_Interface)
					printWrite.println("class " + classes_root[i].name() + " <<interface>> {");
				else if (classes_root[i].isAbstract())
					printWrite.println("abstract class " + classes_root[i].name() + "{");
				else
					printWrite.println("class " + classes_root[i].name() + "{");
				
				ArrayList<String> method_List_GetterSetter = new ArrayList<String>();	
				for (int k=0; k < attr.length; k++) {
									
					// Java Style Public Attributes set as "setters and getters"	
					if (attr[k].isPrivate()) {
						String getter_Method = "get" + attr[k].name().toString();
						String setter_Method = "set" + attr[k].name().toString();
						
						// Put getter and setter methods,has to be excluded while printing
						for (int j=0; j < methods.length; j++) {
							if (methods[j].isPublic()){
								if((methods[j].name().equalsIgnoreCase(getter_Method)) || (methods[j].name().equalsIgnoreCase(setter_Method)))
									method_List_GetterSetter.add(methods[j].name());
							}	
						}
						boolean is_Variable_Set = false;
						for (int j=0; j < methods.length; j++) {
							if((methods[j].name().equalsIgnoreCase(getter_Method)) || (methods[j].name().equalsIgnoreCase(setter_Method)))
							{
								printWrite.println("+" + attr[k].name() + ":" + attr[k].type().typeName());
								is_Variable_Set = true;
								break;
							}
						}	
						if (!is_Variable_Set){
							
							if (!((attr[k].type().typeName()).equals("ArrayList") ||
								(attr[k].type().typeName()).equals("List") ||
								(attr[k].type().typeName()).equals("Collection"))) {
								if (!(attr[k].type().isPrimitive()))
									if (attr[k].isStatic())
										printWrite.println("{static} -" + attr[k].name() + ":" + attr[k].type().typeName() + attr[k].type().dimension());
									else
										printWrite.println("-" + attr[k].name() + ":" + attr[k].type().typeName() + attr[k].type().dimension());
								else
									//to add condition to check if its Collection, ArrayList - as parameterized type function
									if (attr[k].isStatic())
										printWrite.println("{static} -" + attr[k].name() + ":" + attr[k].type());
									else
										printWrite.println("-" + attr[k].name() + ":" + attr[k].type());
							}
						}
					}
					
					// Print only public attr
					if((attr[k].isPublic())) {
						
						if (!((attr[k].type().typeName()).equals("ArrayList") ||
							(attr[k].type().typeName()).equals("List") ||
							(attr[k].type().typeName()).equals("Collection"))) {
							if (!(attr[k].type().isPrimitive()))
								if (attr[k].isStatic())
									printWrite.println("{static} +" + attr[k].name() + ":" + attr[k].type().typeName() + attr[k].type().dimension());
								else
									printWrite.println("+" + attr[k].name() + ":" + attr[k].type().typeName() + attr[k].type().dimension());
							else
								// to check if its Collection, ArrayList - as parameterized type function
								if (attr[k].isStatic())
									printWrite.println("{static} +" + attr[k].name() + ":" + attr[k].type());
								else
									printWrite.println("+" + attr[k].name() + ":" + attr[k].type());
						}
					}
				}
				
				
				for (int z=0; z < constr.length; z++){
					
					String javaFPath = Path_code + classes_root[i].name() + ".java";
					boolean include_Cons = false;
					boolean cons_Exist = check_Explicit_Constructor(javaFPath, constr[z].name());
					if (cons_Exist && constr[z].isPublic())
						include_Cons = true;
						
					
					Parameter[] params = constr[z].parameters();
					if (!((constr[z].isPublic()) && (params.length == 0)) || include_Cons) {
						if (constr[z].isPublic() || constr[z].isPrivate()) {
							if (constr[z].isPublic())
								if (constr[z].isStatic())
									printWrite.print("{static} +" + constr[z].name() + "(");
								else	
									printWrite.print("+" + constr[z].name() + "(");
							if (constr[z].isPrivate())
								if (constr[z].isStatic())
									printWrite.print("{static} -" + constr[z].name() + "(");
							else	
								printWrite.print("-" + constr[z].name() + "(");	

							for (int l=0; l < params.length; l++) {
								if (!((params[l].type().typeName()).equals("ArrayList") ||
									(params[l].type().typeName()).equals("List") ||
									(params[l].type().typeName()).equals("Collection"))) {
										if (l != params.length - 1)
											
											if (!(params[l].type().isPrimitive()))
												printWrite.print(params[l].name() + ":" + params[l].type().typeName() + params[l].type().dimension() + ", ");
											else
												printWrite.print(params[l].name() + ":" + params[l].type() + ", ");
										else
											//if (interfaceClass_list.contains(params[l].type().typeName()))
											if (!(params[l].type().isPrimitive()))
												printWrite.print(params[l].name() + ":" + params[l].type().typeName() + params[l].type().dimension());
											else
												printWrite.print(params[l].name() + ":" + params[l].type());
								}
							}
							printWrite.println(")");
						}
					}
				}
				
				ArrayList<String> methodsList = new ArrayList<String>();
				for (int j=0; j < methods.length; j++) {
					methodsList.add(methods[j].name());
				}
				
				for (int j=0; j < methods.length; j++) {
					
					if (!method_List_GetterSetter.contains(methods[j].name()))
					{		
						Parameter[] params = methods[j].parameters();
						////
						String includeMethod = null;
						String javaFPath = Path_code + classes_root[i].name() + ".java";
						String returnVal = getRetTypeStat(javaFPath, methodsList);
						
						if (!(returnVal.equals("")))
							includeMethod = returnVal;
						
						if (methods[j].isPublic() || methods[j].name().equals(includeMethod)) {
							if (methods[j].isPublic()) {
								if (methods[j].isStatic())
									printWrite.print("{static} +" + methods[j].name() + "(");
								else if (methods[j].isAbstract())
									printWrite.print("{abstract} +" + methods[j].name() + "(");
								else
									printWrite.print("+" + methods[j].name() + "(");
							}
							
							if (methods[j].isPrivate()) {
								if (methods[j].isStatic())
									printWrite.print("{static} -" + methods[j].name() + "(");
								else if (methods[j].isAbstract())
									printWrite.print("{abstract} -" + methods[j].name() + "(");
								else
									printWrite.print("-" + methods[j].name() + "(");
							}

							for (int l=0; l < params.length; l++) {
								
								if (!((params[l].type().typeName()).equals("ArrayList") ||
								(params[l].type().typeName()).equals("List") ||
								(params[l].type().typeName()).equals("Collection"))) {
									if (l != params.length - 1)
									
										if (!(params[l].type().isPrimitive()))
											printWrite.print(params[l].name() + ":" + params[l].type().typeName() + params[l].type().dimension() + ", ");
										else
											printWrite.print(params[l].name() + ":" + params[l].type() + ", ");
										else
										
											if (!(params[l].type().isPrimitive()))
												printWrite.print(params[l].name() + ":" + params[l].type().typeName() + params[l].type().dimension());
											else
												printWrite.print(params[l].name() + ":" + params[l].type());
								}
							}
							if (methods[j].returnType().isPrimitive())
								printWrite.println("):" + methods[j].returnType());
							else
								printWrite.println("):" + methods[j].returnType().typeName());
						}
					}
				}// for iteration on array methods 
	 
				printWrite.println("}"); // closing brace for class.
				printWrite.flush();
			} //for loop on array of classes.
			if (!(packages_names.name().equals("")))
				printWrite.println("}"); // closing brace for package
			printWrite.println("@enduml");
			printWrite.flush();
			outputFileS.close();
			printWrite.close();
			
		} //try	in class Parser
		catch(IOException e){}
		return true;
    }// start
	
	
	
	public static String checkInClass(String filename, String methodName, ArrayList<String> classList) {
		try (BufferedReader bfr = new BufferedReader(new FileReader(filename))) {
			String string = null;
			//String main_str = "main";
			while ((string = bfr.readLine()) != null) {
				//if (str.contains(main_str)) {
				if (string.contains(methodName)) {
					do {
						string = bfr.readLine();
						for (int i=0; i < classList.size(); i++)
							if (string.contains(classList.get(i)))
								return classList.get(i);
					} while((string != null) || !(string.contains("}")));
				}
				//string = bfr.readLine();
			}
		}
		catch(Exception e) {
		}
		return "";
	}
	
	public static String get_Generic_Type(String fileName, String objectName) {
		
		try (BufferedReader bfr = new BufferedReader(new FileReader(fileName))) {
			String string = null;
			string = bfr.readLine();
			if (objectName.equals("b"))
				objectName = objectName + ";";
			if (objectName.equals("a"))
				objectName = objectName + ";";
			while (string != null && string != "") {
				
				if (string.contains(objectName)) {
					//return string;
					string = string.substring(string.indexOf("<")+1,string.indexOf(">"));
					return string;
				}
				string = bfr.readLine();
			}
		}
		catch(Exception e) {
		}
		return "";
		
	}
	
	public static boolean check_Explicit_Constructor(String fileName, String consName) 
	{
		try (BufferedReader bfr = new BufferedReader(new FileReader(fileName))) {
			String string = null;
			//string = bfr.readLine();
			String consName1 = consName + "()";
			String consName2 = consName + " ()";
			while ((string = bfr.readLine()) != null) {
				if (string.contains(consName1) || string.contains(consName2)) {
					return true;
				}
			}
		}
		catch(Exception e) {
		}
		return false;
		//return objectName;
    }
	
	public static String getRetTypeStat(String fileName, ArrayList<String> methodList) {
		try (BufferedReader bfr = new BufferedReader(new FileReader(fileName))) {
			String str = null;
			//str = bfr.readLine();
			
			while ((str = bfr.readLine()) != null) {
				if (str.contains("return")) {
					for (int i=0; i < methodList.size(); i++)
						if (str.contains(methodList.get(i)))
							return methodList.get(i);
				}
			}
		}
		catch(Exception e) {
		}
		return "";
		
	}
	
	
} // Class Parser