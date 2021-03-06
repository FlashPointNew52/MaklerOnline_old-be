package com.zavrus.app;
import static spark.Spark.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hibernate.User;
import hibernate.Billing;
import hibernate.HibernateUtil;
import org.hibernate.Session;

import org.json.JSONObject;

import java.net.URLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import java.text.SimpleDateFormat;
import java.lang.StringBuilder;

import java.util.HashMap;
import utility.JsonTrans;
import utility.Email;
import utility.sesUser;

import java.net.HttpURLConnection;
import java.net.URL;

import org.hibernate.HibernateException;


public class App{

    public static void main( String[] args ){
        HashMap<String, Object> userSession = new HashMap<>();
        port(19201);
        enableCORS("*","*","*");
        Logger logger = LoggerFactory.getLogger(App.class);

        post("/newUser", (req, res) -> {
            Session session = HibernateUtil.getSessionFactory().openSession();
            HashMap<String, String> result = new HashMap<>();
            try{
                session.beginTransaction();
                int results = session.createQuery("select au from User au where au.email = :email")
                                                .setParameter("email", req.queryParams("mail")).list().size();
                if(results == 0){
                    User user = new User();
                    Date dt = new Date();
                    String password = generatePass();
                    user.setMail(req.queryParams("mail").toLowerCase());
                    user.setPass(password);
                    user.setTime(dt);
                    user.setFL(new ArrayList<Integer>());
                    session.save(user);
                    if(Email.send(req.queryParams("mail"), "Регистрация на сайте zavrus.com",
                                "Ваш пароль для входа в систему: "+password))
                                result.put("send", "yes");
                    else result.put("send", "no");
                    session.getTransaction().commit();
                    HibernateUtil.shutdown();
                    result.put("result", "OK");
                } else {
                    result.put("result", "Not");
                    result.put("Rison", "Already registration");
                }
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("Rison", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            }finally {
                session.close();
            }

            return result;
        }, new JsonTrans());

        get("/hello", (req, res) -> {
            return "This is not for you, BITCH!!!!!";
        });

        post("/getUser", (req, res) -> {
            Session session = HibernateUtil.getSessionFactory().openSession();
            HashMap<String, Object> result = new HashMap<>();
            try{
                session.beginTransaction();
                List users = (ArrayList<User>)session.createQuery("select au from User au where au.email = :email AND au.password = :pass")
                .setParameter("email", req.queryParams("mail")).setParameter("pass", req.queryParams("pass")).list();
                if (users.size() == 1 && !req.queryParams("mail").equals("") && !req.queryParams("pass").equals("")){
                    req.session(true);
                    User user = (User) users.get(0);
                    session.getTransaction().commit();
                    HibernateUtil.shutdown();
                    userSession.put(req.session().id(), new sesUser(user.getId(), new Date()));
                    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm");
                    result.put("result", "OK");
                    result.put("session", req.session().id());
                    result.put("id", user.getId());
                    result.put("name", user.getName());
                    result.put("favourite", user.getFL());
                    result.put("time", ft.format(user.getTime()).toString());

                } else{
                    result.put("result", "Not");
                    result.put("reason", "User not found");
                }
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("Rison", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }

            return result;
        }, new JsonTrans());

        post("/getSession", (req, res) -> {
            Session session = HibernateUtil.getSessionFactory().openSession();
            HashMap<String, Object> result = new HashMap<>();
            try{
                session.beginTransaction();
                if(userSession.containsKey(req.queryParams("session"))){
                    Date time = ((sesUser)userSession.get(req.queryParams("session"))).date;
                    Integer id = ((sesUser)userSession.get(req.queryParams("session"))).id;
                    if( (new Date().getTime()) - time.getTime() < 3600000){
                        result.put("result", "Ok");
                        User user = (User)session.createQuery("select au from User au where au.userId = :userId")
                                .setParameter("userId", id).list().get(0);
                        session.getTransaction().commit();
                        HibernateUtil.shutdown();
                        result.put("id", user.getId());
                        result.put("name", user.getName());
                        result.put("favourite", user.getFL());
                        result.put("time", user.getTime());
                        ((sesUser)userSession.get(req.queryParams("session"))).date = new Date();
                    } else {
                        userSession.remove(req.queryParams("session"));
                        result.put("result", "Not");
                        result.put("reason", "Time end");
                    }
                }
                else {
                    result.put("result", "Not");
                    result.put("reason", "No session");
                }
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("Rison", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }
            return result;
        }, new JsonTrans());

        post("/closeSession", (req, res) -> {
            HashMap<String, Object> result = new HashMap<>();
            if(userSession.containsKey(req.queryParams("session"))){
                userSession.remove(req.queryParams("session"));
                result.put("result", "Ok");
            }
            else {
                result.put("result", "Not");
            }
            return result;
        }, new JsonTrans());

        post("/prePay", (req, res) -> {
            HashMap<String, Object> result = new HashMap<>();
            Session session = HibernateUtil.getSessionFactory().openSession();
            try{
                session.beginTransaction();
                Billing bill = new Billing();
                Date dt = new Date();

                bill.setAmount(Integer.parseInt(req.queryParams("summ")));
                bill.setState(0);
                bill.setUser(Integer.parseInt(req.queryParams("userId")));
                bill.setTime(new Date());
                session.save(bill);
                session.getTransaction().commit();
                HibernateUtil.shutdown();

                result.put("WMI_MERCHANT_ID", "127278326049");
                result.put("WMI_PAYMENT_NO", bill.getId());
                result.put("WMI_PAYMENT_AMOUNT", bill.getAmount());
                result.put("WMI_CURRENCY_ID", "643");
                result.put("WMI_DESCRIPTION", "Оплата доступа на Zavrus.com");
                result.put("WMI_SUCCESS_URL", "http://xn--b1adacaabaehsdbwnyeec1a7dwa0toa.xn--p1ai/#/pay?isPaid=true");
                result.put("WMI_FAIL_URL", "http://xn--b1adacaabaehsdbwnyeec1a7dwa0toa.xn--p1ai/#/pay?isPaid=false");

                ArrayList<String> params = new ArrayList<String>();
                params.add("127278326049");
                params.add(bill.getId().toString());
                params.add(bill.getAmount().toString());
                params.add("643");
                params.add("BASE64:" + Base64.encodeBase64(new String("Оплата доступа на ЕженедельникНедвижимость.рф").getBytes())+"==");
                params.add("http://zavrus.com:3000/paid");
                params.add("http://zavrus.com:3000/fail");
                params.sort(String::compareToIgnoreCase);
                String key = "34347132455758456944425d665a4b447774377c554745554d576e";

                byte[] bytes = Base64.encodeBase64(md5Apache(new String((String.join("", params) + key).getBytes(), "Windows-1252")));
                result.put("WMI_SIGNATURE", bytes);
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("Rison", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }

            return result;
        }, new JsonTrans());

        post("/pay_rezult", (req, res) -> {
            HashMap<String, Object> result = new HashMap<>();
            Session session = HibernateUtil.getSessionFactory().openSession();
            try{
                session.beginTransaction();
                int summ = (int)Math.floor(Double.parseDouble(req.queryParams("WMI_PAYMENT_AMOUNT")));
                int bilId = Integer.parseInt(req.queryParams("WMI_PAYMENT_NO"));
                String state =  req.queryParams("WMI_ORDER_STATE");
                Billing bill = (Billing)session.createQuery("select au from Billing au where au.billingID = :bilId")
                        .setParameter("bilId", bilId).list().get(0);
                if(state.equals("Accepted") && bill.getState() == 0){
                    bill.setState(1);
                    session.saveOrUpdate(bill);
                    User user = (User)session.createQuery("select au from User au where au.userId = :userId")
                            .setParameter("userId", bill.getUser()).list().get(0);
                    user.addDays(summToDays(summ));
                    System.out.println(user.getTime());
                    System.out.println(user.getId());
                    System.out.println(bill.getState());
                    session.saveOrUpdate(user);
                    session.getTransaction().commit();
                    HibernateUtil.shutdown();

                } else
                    bill.setState(2);
                result.put("WMI_RESULT", "OK");
            } catch (HibernateException e) {
                result.put("WMI_RESULT", "RETRY");
                result.put("WMI_DESCRIPTION", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }
            return result;
        }, new JsonTrans());

        post("/editFavList", (req, res) -> {
            HashMap<String, Object> result = new HashMap<>();
            Session session = HibernateUtil.getSessionFactory().openSession();
            try{
                session.beginTransaction();
                int userID = Integer.parseInt(req.queryParams("userId"));
                int reltyID = Integer.parseInt(req.queryParams("reltyId"));
                boolean isAdd = Boolean.valueOf(req.queryParams("isAdd"));
                User user = (User)session.createQuery("select au from User au where au.userId = :userId")
                        .setParameter("userId", userID).list().get(0);
                if(isAdd)
                    user.addFav(reltyID);
                else user.removeFav(reltyID);
                session.saveOrUpdate(user);
                session.getTransaction().commit();
                HibernateUtil.shutdown();
                result.put("newList", user.getFL());
                result.put("result", "OK");
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("error", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }
            return result;
        }, new JsonTrans());

        get("/vkUser", (req, res) -> {
            HashMap<String, Object> result = new HashMap<>();
            URLConnection connection = new URL("https://api.vk.com/method/users.get?user_ids="
                                        +req.queryParams("userId")+"&v=5.62").openConnection();

            InputStream is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;

            StringBuilder sb = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);
            reader.close();
            JSONObject jsonObj = new JSONObject(sb.toString());

            Session session = HibernateUtil.getSessionFactory().openSession();
            try{
                session.beginTransaction();
                List<User> users = session.createQuery("select au from User au where au.social_id = :social_id AND au.social_type = :social_type")
                        .setParameter("social_id", jsonObj.getJSONArray("response").getJSONObject(0).get("id")).setParameter("social_type", "VK").list();
                SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm");
                if(users.size() != 0){
                    userSession.put(req.session().id(), new sesUser(users.get(0).getId(), new Date()));
                    result.put("isNew", false);
                    result.put("result", "OK");
                    result.put("session", req.session().id());
                    result.put("id", users.get(0).getId());
                    result.put("name", users.get(0).getName());
                    result.put("favourite", users.get(0).getFL());
                    result.put("time", ft.format(users.get(0).getTime()).toString());
                } else{
                    User user = new User();
                    Date dt = new Date();
                    user.setMail("");
                    user.setPass("");
                    user.setSocial("VK");
                    user.setSocial_ID(jsonObj.getJSONArray("response").getJSONObject(0).getInt("id"));
                    user.setTime(dt);
                    user.setFL(new ArrayList<Integer>());
                    session.save(user);
                    userSession.put(req.session().id(), new sesUser(user.getId(), new Date()));
                    result.put("isNew", true);
                    result.put("result", "OK");
                    result.put("session", req.session().id());
                    result.put("id", user.getId());
                    result.put("name", user.getName());
                    result.put("favourite", user.getFL());
                    result.put("time", ft.format(user.getTime()).toString());
                }
                session.getTransaction().commit();
                HibernateUtil.shutdown();

            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("error", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            } finally {
                session.close();
            }
            return result;
        }, new JsonTrans());

        post("/updatePassword", (req, res) -> {
            Session session = HibernateUtil.getSessionFactory().openSession();
            HashMap<String, String> result = new HashMap<>();
            try{
                session.beginTransaction();
                List<User> user = session.createQuery("select au from User au where au.email = :email")
                                                .setParameter("email", req.queryParams("email")).list();
                if(user.size() == 1){
                    String password = generatePass();
                    user.get(0).setPass(password);
                    session.save(user.get(0));
                    if(Email.send(req.queryParams("email"), "Обновление пароля ЕженедельникНедвижимость.рф",
                                "В целях безопасности мы переодически обновляем ваш пароль доступа. Ваш новый пароль: "+password))
                                result.put("send", "yes");
                    else result.put("send", "no");
                    session.getTransaction().commit();
                    HibernateUtil.shutdown();
                    result.put("result", "OK");
                } else {
                    result.put("result", "Not");
                    result.put("Rison", "Not find user");
                }
            } catch (HibernateException e) {
                result.put("result", "Not");
                result.put("Rison", "Database Error: "+ e.getMessage());
                e.printStackTrace();
            }finally {
                session.close();
            }

            return result;
        }, new JsonTrans());

        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.body("Ex:" + exception.getMessage());
            exception.printStackTrace();
        });
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            response.type("application/json");
        });
    }

    private static String generatePass(){
        char[] chars = "ABCDEFJHIJKLMNOPQRSTVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    private static byte[] md5Apache(String st) {
        return DigestUtils.md5(st);
    }

    private static int summToDays(int summ){
        switch(summ) {
            case 50: return 1;
            case 150: return 3;
            case 350: return 7;
            case 1500: return 31;
            default: return 0;
        }
    }


}
