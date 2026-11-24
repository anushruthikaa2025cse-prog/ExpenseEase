package service;

import java.io.IOException;
import java.sql.SQLException;

// OOP: Interface for classes that export data to a file.
public interface Exportable {

    // Every exporter must write the report to a file.
    void exportData(int userId, String username, String fileName)
            throws IOException, SQLException;

    // OOP: Default method - every exporter gets this for free.
    default String getFileType() {
        return "Excel report (.xlsx)";
    }

    // OOP: Static method - small helper used by the XLSX exporter.
    static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        String result = text.replace("&", "&amp;");
        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replace("\"", "&quot;");
        return result;
    }
}
