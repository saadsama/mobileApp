package com.example.locationapp.model;

    import java.util.Date;

    public class Evaluation {
        private String id;
        private Client client;
        private Offre offre;
        private int score;
        private String commentaire;
        private Date dateEvaluation;

        // Default constructor
        public Evaluation() {
        }

        // Parameterized constructor
        public Evaluation(Client client, Offre offre, int score,
                          String commentaire, Date dateEvaluation) {
            this.client = client;
            this.offre = offre;
            this.score = score;
            this.commentaire = commentaire;
            this.dateEvaluation = dateEvaluation;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public Offre getOffre() {
            return offre;
        }

        public void setOffre(Offre offre) {
            this.offre = offre;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getCommentaire() {
            return commentaire;
        }

        public void setCommentaire(String commentaire) {
            this.commentaire = commentaire;
        }

        public Date getDateEvaluation() {
            return dateEvaluation;
        }

        public void setDateEvaluation(Date dateEvaluation) {
            this.dateEvaluation = dateEvaluation;
        }
    }
