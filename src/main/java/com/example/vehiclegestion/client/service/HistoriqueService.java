package com.example.vehiclegestion.client.service;

import com.example.vehiclegestion.client.doa.HistoriqueClientDAO;
import com.example.vehiclegestion.client.model.HistoriqueItem;
import java.util.List;
import java.util.Map;

public class HistoriqueService {

    private final HistoriqueClientDAO historiqueDAO;

    public HistoriqueService() {
        this.historiqueDAO = new HistoriqueClientDAO();
    }

    public List<HistoriqueItem> getHistoriqueClient(int clientId, String periode, String typeFiltre) {
        return historiqueDAO.getHistoriqueComplet(clientId, periode, typeFiltre);
    }

    public Map<String, Object> getStatistiquesClient(int clientId) {
        return historiqueDAO.getStatistiques(clientId);
    }


    public List<HistoriqueItem> getDernieresActions(int clientId, int limit) {
        // Appeler getHistoriqueComplet avec une période large et limiter les résultats
        List<HistoriqueItem> historique = historiqueDAO.getHistoriqueComplet(clientId, "Toutes périodes", "Tous");
        if (historique.size() > limit) {
            return historique.subList(0, Math.min(limit, historique.size()));
        }
        return historique;
    }


    public Map<String, Integer> getRepartitionParMois(int clientId) {
        // Implémenter si besoin pour graphiques
        return Map.of();
    }
}