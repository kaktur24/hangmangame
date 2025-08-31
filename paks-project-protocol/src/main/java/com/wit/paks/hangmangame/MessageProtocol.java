package com.wit.paks.hangmangame;

public enum MessageProtocol {
    SET_PLAYER_NAME ("snm:"),
    GET_PLAYER_NAME ("gnm:"),
    PLACED_IN_QUEUE("pqu:"),
    INPUT_PHRASE ("iph:"),
    WAIT_FOR_OTHERS("wfo:"),
    START_ROUND ("str:"),
    UPDATE_OBSERVER_STATUS ("uos:"),
    UPDATE_PHRASE_STATUS ("ups:"),
    UPDATE_MISTAKES_STATUS ("ums:"),
    GUESS_LETTER  ("gsl:"),
    GUESS_VERDICT ("gsv:"),
    PLAYER_FINISHED_ROUND ("pfr:"),
    ALL_FINISHED_ROUND ("afr:"),
    CONFIRM_FINISHING_ROUND  ("cfr:"),
    FINISH_GAME("fgm:"),
    PLAY_NEXT_GAME("png:"),
    END_CONNECTION("ecn:");

    private final String strRep;

    MessageProtocol(String strRep) {
        this.strRep = strRep;
    }

    public String toString() {
        return strRep;
    }

    public static MessageProtocol getMessageProtocol(String strRep) {
        for(MessageProtocol prt : MessageProtocol.values()) {
            if(prt.strRep.equals(strRep))
                return prt;
        }
        return null;
    }
}
