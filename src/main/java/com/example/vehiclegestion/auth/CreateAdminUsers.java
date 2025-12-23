package com.example.vehiclegestion.auth;

import com.example.vehiclegestion.auth.model.Utilisateur;

public class CreateAdminUsers {

    public static void main(String[] args) {
        AuthentificationService authService = new AuthentificationService();

        // Création de Khadija
        Utilisateur khadija = new Utilisateur();
        khadija.setNom("Khadija");
        khadija.setPrenom("Admin");
        khadija.setEmail("nada@gmail.com");
        khadija.setMotDePasse("12345678");
        khadija.setRole("admin");

        boolean khadijaSuccess = authService.inscrire(khadija);
        System.out.println("Khadija créé ? " + khadijaSuccess);

        // Création de Aamer
        Utilisateur aamer = new Utilisateur();
        aamer.setNom("Aamer");
        aamer.setPrenom("Admin");
        aamer.setEmail("aamerk@gmail.com");
        aamer.setMotDePasse("12345678");
        aamer.setRole("admin");

        boolean aamerSuccess = authService.inscrire(aamer);
        System.out.println("Aamer créé ? " + aamerSuccess);
    }
}
