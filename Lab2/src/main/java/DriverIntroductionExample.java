import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverIntroductionExample implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DriverIntroductionExample.class.getName());
    private static Driver driver = null;

    public DriverIntroductionExample(String uri, String user, String password, Config config) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void createNode(final String class_1, final String name_1, final String form, final String type) {
        String createNode = "CREATE (p1:" + class_1 + "{name: $name1, form: $form, type: $type})\n" +
                "RETURN p1";
        Map<String, Object> params = new HashMap<>();
        params.put("name1", name_1);
        params.put("form", form);
        params.put("type", type);
        try (Session session = driver.session()) {
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(createNode, params);
                return result.single();
            });
            System.out.println(String.format("Created Node - %s",
                    name_1
            ));
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, createNode + " raised an exception", ex);
            throw ex;
        }
    }


    public void removeNode(final String class_1, final String name_1, final String form, final String type) {
        String removeNode = "MATCH(p1:" + class_1 + "{name: $name1, form: $form, type: $type})\n" +
                "DETACH DELETE p1\n" +
                "RETURN p1";
        Map<String, Object> params = new HashMap<>();
        params.put("name1", name_1);
        params.put("form", form);
        params.put("type", type);
        try (Session session = driver.session()) {
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(removeNode, params);
                return result.single();
            });
            System.out.println(String.format("Removed Node - %s",
                    name_1
            ));
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, removeNode + " raised an exception", ex);
            throw ex;
        }
    }

    public void createFriendship(final String name_1, final String name_11, final String form1,  final String type1,
                                 final String rel, final String name_2, final String name_22, final String form2, final String type2) {
        String createFriendshipQuery = "MATCH(p1:" + name_1 + "{name: $name1, form: $form1, type: $type1})," +
                "(p2:" + name_2 + "{name: $name2, form: $form2, type: $type2})\n" +
                "CREATE (p1)-[rel:" + rel + "]->(p2)\n" +
                "RETURN p1, p2;";
        Map<String, Object> params = new HashMap<>();
        params.put("name1", name_11);
        params.put("form1", form1);
        params.put("type1", type1);
        params.put("name2", name_22);
        params.put("form2", form2);
        params.put("type2", type2);

        try (Session session = driver.session()) {
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(createFriendshipQuery, params);
                return result.single();
            });
            System.out.println(String.format("Created friendship between: %s - %s -> %s",
                    name_11,
                    rel,
                    name_22
            ));
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, createFriendshipQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public void removeFriendship(final String name_1, final String name_11, final String form1,  final String type1,
                                 final String rel, final String name_2, final String name_22, final String form2, final String type2) {
        String removeFriendshipQuery = "MATCH(p1:" + name_1 + "{name: $name1, form: $form1, type: $type1})" +
                "-[" + rel + "]->(p2:" + name_2 + "{name: $name2, form: $form2, type: $type2})\n" +
                "DELETE " + rel + "\n" +
                "RETURN p1, p2";

        Map<String, Object> params = new HashMap<>();
        params.put("name1", name_11);
        params.put("form1", form1);
        params.put("type1", type1);
        params.put("name2", name_22);
        params.put("form2", form2);
        params.put("type2", type2);

        try (Session session = driver.session()) {
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(removeFriendshipQuery, params);
                return result.single();
            });
            System.out.println(String.format("Removed friendship between: %s - %s -> %s",
                    name_11,
                    rel,
                    name_22
            ));
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, removeFriendshipQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public static void get1(final String name1, final String name2, final String name3) {
        String readPersonByNameQuery = "MATCH (p1:Screen{name:\'" + name1 + "\', form:\'" + name2 + "\', type:\'" + name3 + "\'})-[re]->(p2)\n" +
                "RETURN p1.name, p1.form, p2.name, p2.form, type(re) as re1";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(readPersonByNameQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    String pods = "";
                    if (record.get("re1").asString().equals("нажать")) pods = "tap";
                    else if (record.get("re1").asString().equals("заполнить") || record.get("re1").asString().equals("Заполнить")) pods = "fill";
                    else if (record.get("re1").asString().equals("отметить")) pods = "tick";
                    System.out.println(String.format("Название Screen:\"%s\" || Псевдоним:\"%s\" || Имя элемента:\"%s\" || Псевдоним элемента:\"%s\" || Действие:\"%s_%s_%s\"",
                            record.get("p1.name").asString(), record.get("p1.form").asString(), record.get("p2.name").asString(),
                            record.get("p2.form").asString(), record.get("p1.name").asString(), record.get("p2.name").asString(), pods));
                }
                return result;
            });
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPersonByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public static void get3() {
        String readPersonByNameQuery = "MATCH (p1)-[re]->(p2)\n" +
                "RETURN p1.name, p1.form, p1.type, type(re) as re1, p2.name, p2.form, p2.type";

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(readPersonByNameQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    String pods = "";
                    if (record.get("re1").asString().equals("нажать")) pods = "tap";
                    else if (record.get("re1").asString().equals("заполнить") || record.get("re1").asString().equals("Заполнить")) pods = "fill";
                    else if (record.get("re1").asString().equals("отметить")) pods = "tick";
                    else if (record.get("re1").asString().equals("Переход") || record.get("re1").asString().equals("переход")) pods = "continue";
                    System.out.println(String.format("Название Screen:\"%s\" || Псевдоним:\"%s\" || Описание:\"%s\" || Имя элемента:\"%s\" || Псевдоним элемента:\"%s\" || Тип:\"%s\" || Действие:\"%s_%s_%s\"",
                            record.get("p1.name").asString(), record.get("p1.form").asString(), record.get("p1.type").asString(), record.get("p2.name").asString(),
                            record.get("p2.form").asString(), record.get("p1.type").asString(), record.get("p1.name").asString(), record.get("p2.name").asString(), pods));
                }
                return result;
            });
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPersonByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public static void get4() {
        String readPersonByNameQuery = "MATCH (n)\n" +
                "RETURN n.intID, n.name\n" +
                "ORDER BY n.intID";

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(readPersonByNameQuery);
                int i = 1;
                ArrayList<Record> arrRec = new ArrayList<Record>();
                while (result.hasNext()) {
                    Record record = result.next();
                    String temp = "";
                    if (record.get("n.intID").isNull()) break;
                    System.out.println(i);
                    System.out.println(record.get("n.name").asString());
                    System.out.print("\n");
                    i++;
                }
                return result;
            });
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPersonByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public static void get42() {
        String readPersonByNameQuery = "MATCH (n)\n" +
                "RETURN n.intID2, n.name\n" +
                "ORDER BY n.intID2";

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(readPersonByNameQuery);
                int i = 1;
                ArrayList<Record> arrRec = new ArrayList<Record>();
                while (result.hasNext()) {
                    Record record = result.next();
                    String temp = "";
                    if (record.get("n.intID2").isNull()) break;
                    System.out.println(i);
                    System.out.println(record.get("n.name").asString());
                    System.out.print("\n");
                    i++;
                }
                return result;
            });
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPersonByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }

    public static void get5(String name1, String name2) {
        String readPersonByNameQuery = "MATCH (:"+name1+"{name: '"+name2+ "'})-[r]-(p)\n" +
                "RETURN p.name, type(r) as d";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(readPersonByNameQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    System.out.println(String.format("Connection:\"%s\" >>> next Node: \"%s\"", record.get("d").asString(), record.get("p.name").asString()));
                }
                return result;
            });
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPersonByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }
    public static void menu(DriverIntroductionExample app) {
        int selector;
        Scanner sc = new Scanner(System.in);
        selector = sc.nextInt();
        String a,b,c,d,e,f,g,h,l;
        switch (selector) {
            case 1:
                System.out.println("Конструктор Node:\nВведите класс, form, name, type");
                sc.nextLine();
                a = sc.nextLine();
                b = sc.nextLine();
                c = sc.nextLine();
                d = sc.nextLine();
                app.createNode(a, b, c, d);
                break;
            case 2:
                System.out.println("Remover Node:\nВведите класс, form, name, type");
                sc.nextLine();
                a = sc.nextLine();
                b = sc.nextLine();
                c = sc.nextLine();
                d = sc.nextLine();
                app.removeNode(a, b, c, d);
                break;
            case 3:
                System.out.println("Create Node:\nВведите класс_первый, form_first, name_first, type_first," +
                        " отношение, класс_второй, form_second, name_second, type_second");
                a = sc.nextLine();
                a = sc.nextLine();
                b = sc.nextLine();
                c = sc.nextLine();
                d = sc.nextLine();
                e = sc.nextLine();
                f = sc.nextLine();
                g = sc.nextLine();
                h = sc.nextLine();
                l = sc.nextLine();
                app.createFriendship(a, b, c, d, e, f, g, h, l);
                break;
            case 4:
                System.out.println("Remover Node:\nВведите класс_первый, form_first, name_first, type_first," +
                        " отношение, класс_второй, form_second, name_second, type_second");
                a = sc.nextLine();
                a = sc.nextLine();
                b = sc.nextLine();
                c = sc.nextLine();
                d = sc.nextLine();
                e = sc.nextLine();
                f = sc.nextLine();
                g = sc.nextLine();
                h = sc.nextLine();
                l = sc.nextLine();
                app.removeFriendship(a, b, c, d, e, f, g, h, l);
                break;
            case 5:
                get1("Главный экран", "screen", "Main");
                break;
            case 6:
                get1("Экран фильтров", "screen", "Filter");
                break;
            case 7:
                get3();
                break;
            case 8:
                get5("Screen", "Экран с фильмом");
                break;
            case 9:
                get4();
                break;
            case 10:
                get42();
                break;
            case 11:
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println("|                              МЕНЮ                                         |");
                System.out.println("|                      1 - Добавить Node                                    |");
                System.out.println("|                      2 - Удалить Node                                     |");
                System.out.println("|                      3 - Создать connection                               |");
                System.out.println("|                      4 - Удалить connection                               |");
                System.out.println("|                      5 - Вывести все элементы Главного экрана             |");
                System.out.println("|                      6 - Вывести все элементы Оформления заказа           |");
                System.out.println("|                      7 - Вывести все элементы и их описания               |");
                System.out.println("|                      8 - Вывести путь                                     |");
                System.out.println("|                      9 - Вывести последовательность действий 1            |");
                System.out.println("|                      10 - Вывести последовательность действий 2           |");
                System.out.println("|                      11 - Вывод меню                                      |");
                System.out.println("-----------------------------------------------------------------------------");
                break;
            default:
                System.out.println("Ошибка ввода!");
        }
    }

    public static void main(String... args) throws Exception {
        String uri = "neo4j+s://216a71b9.databases.neo4j.io";
        String user = "neo4j";
        String password = "TkcnYbn1JyrghMOySoOmPuzVYzPrzkJvO8eAAGJpiH0";

        try (DriverIntroductionExample app = new DriverIntroductionExample(uri, user, password, Config.defaultConfig())) {
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("|                              МЕНЮ                                         |");
            System.out.println("|                      1 - Добавить Node                                    |");
            System.out.println("|                      2 - Удалить Node                                     |");
            System.out.println("|                      3 - Создать connection                               |");
            System.out.println("|                      4 - Удалить connection                               |");
            System.out.println("|                      5 - Вывести все элементы Главного экрана             |");
            System.out.println("|                      6 - Вывести все элементы Оформления заказа           |");
            System.out.println("|                      7 - Вывести все элементы и их описания               |");
            System.out.println("|                      8 - Вывести путь                                     |");
            System.out.println("|                      9 - Вывести последовательность действий 1            |");
            System.out.println("|                      10 - Вывести последовательность действий 2           |");
            System.out.println("|                      11 - Вывод меню                                      |");
            System.out.println("-----------------------------------------------------------------------------");
            while (true) {
                try {
                    menu(app);
                }
                catch ( Exception e)
                {
                    System.out.print("\n\n\n");
                    System.out.print("Ошибка ввода.\n");
                    System.out.print("\n\n\n");
                    System.out.println("-----------------------------------------------------------------------------");
                    System.out.println("|                              МЕНЮ                                         |");
                    System.out.println("|                      1 - Добавить Node                                    |");
                    System.out.println("|                      2 - Удалить Node                                     |");
                    System.out.println("|                      3 - Создать connection                               |");
                    System.out.println("|                      4 - Удалить connection                               |");
                    System.out.println("|                      5 - Вывести все элементы Главного экрана             |");
                    System.out.println("|                      6 - Вывести все элементы Оформления заказа           |");
                    System.out.println("|                      7 - Вывести все элементы и их описания               |");
                    System.out.println("|                      8 - Вывести путь                                     |");
                    System.out.println("|                      9 - Вывести последовательность действий 1            |");
                    System.out.println("|                      10 - Вывести последовательность действий 2           |");
                    System.out.println("|                      11 - Вывод меню                                      |");
                    System.out.println("-----------------------------------------------------------------------------");
                    System.out.print("\n");
                }
                finally{
                    System.out.print("");
                }
            }
        }
    }
}