package agh.cs.projekt.models;

public enum CountryEnum {
    POLAND,
    GERMANY,
    USA,
    RUSSIA,
    CANADA,
    SWITZERLAND,
    AUSTRALIA,
    ARGENTINA,
    CHINA,
    INDIA,
    SLOVAKIA,
    UKRAINE,
    EGYPT;

    @Override
    public String toString() {
        if (this == POLAND){
            return "Polska";
        } else if (this == GERMANY){
            return "Niemcy";
        } else if (this == USA){
            return "USA";
        } else if (this == RUSSIA){
            return "Rosja";
        } else if (this == CANADA){
            return "Kanada";
        } else if (this == SWITZERLAND){
            return "Szwajcaria";
        } else if (this == AUSTRALIA){
            return "Australia";
        } else if (this == ARGENTINA){
            return "Argentyna";
        } else if (this == CHINA){
            return "Chiny";
        } else if (this == INDIA){
            return "Inde";
        } else if (this == SLOVAKIA){
            return "Słowacja";
        } else if (this == UKRAINE){
            return "Ukraina";
        } else if (this == EGYPT){
            return "Egipt";
        } else {
            System.err.println("Country not recognised, enum " + this.ordinal() + ". Check if enum is recognised in the toString() function");
            return "Bład";
        }
    }
}
