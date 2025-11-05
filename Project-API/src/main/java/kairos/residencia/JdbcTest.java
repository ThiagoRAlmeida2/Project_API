package kairos.residencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcTest {

    static final String DB_URL = "jdbc:mysql://localhost:3306/kairos_db";
    static final String USER = "K1NG45";
    static final String PASS = "Thiagomitosis12!";

    public static void main(String[] args) {
        System.out.println("--- Testando Conexão JDBC com MySQL ---");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver JDBC carregado com sucesso.");
        } catch (ClassNotFoundException e) {
            System.out.println("ERRO: Driver MySQL não encontrado no classpath.");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {

            System.out.println("\n✅ SUCESSO: Conexão com o banco de dados estabelecida!");
            System.out.println("URL: " + DB_URL);
            System.out.println("Usuário: " + USER);

            if (conn.isValid(5)) { // 5 segundos de timeout
                System.out.println("Conexão validada.");
            } else {
                System.out.println("Aviso: Conexão estabelecida, mas o método isValid() retornou falso.");
            }

        } catch (SQLException e) {
            System.out.println("\n❌ FALHA NA CONEXÃO:");
            System.out.println("Código do Erro SQL: " + e.getErrorCode());
            System.out.println("Mensagem: " + e.getMessage());

            if (e.getErrorCode() == 1045) {
                System.out.println("\nDICA: Erro 1045 - Acesso negado. Verifique o usuário e a senha.");
            } else if (e.getErrorCode() == 0 || e.getErrorCode() == 2003) {
                System.out.println("\nDICA: Erro 2003/0 - Servidor inacessível. Verifique se o MySQL está rodando na porta 3306.");
            }
        }
    }
}