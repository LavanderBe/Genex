package Genex.tests;

import Genex.entities.User;
import Genex.services.CrudUser;

public class test {
    public static void main(String[] args) {
        User u=new User ("user","mail@mail.com","mdp","admin");
        CrudUser cu=new CrudUser();
        cu.addEntity(u);
    }
}
