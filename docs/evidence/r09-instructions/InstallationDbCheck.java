package it.unifi.drivehub;

/** Controllo della configurazione e delle credenziali demo nella copia d'installazione. */
public class InstallationDbCheck {
    public static void main(String[] args) throws Exception {
        AppContext ctx = AppContext.getInstance();
        try (var conn = ctx.getDatabaseManager().getConnection();
             var stmt = conn.createStatement();
             var rows = stmt.executeQuery("SELECT current_database(), current_user, current_setting('server_version'), EXISTS(SELECT 1 FROM brand WHERE name='R00_SENTINEL')")) {
            if (!rows.next() || !"drivehub".equals(rows.getString(1))
                    || !"drivehub".equals(rows.getString(2))
                    || !rows.getString(3).startsWith("16.") || !rows.getBoolean(4))
                throw new AssertionError("Destinazione installazione/sentinella non corretta");
        }
        String[][] users = {
            {"mario.rossi@email.it", "Cliente"},
            {"paolo.costa@drivehub.it", "Venditore"},
            {"vittorio.manager@drivehub.it", "Manager"}
        };
        for (String[] entry : users) {
            var user = ctx.getAuthService().login(entry[0], "password");
            if (!entry[1].equals(user.getRoleDisplayName()))
                throw new AssertionError("Credenziali/ruolo demo non corrispondenti");
        }
        System.out.println("INSTALLATION DB AND THREE DEMO LOGINS SUCCESS");
    }
}
