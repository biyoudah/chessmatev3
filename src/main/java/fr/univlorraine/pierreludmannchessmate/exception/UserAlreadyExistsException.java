package fr.univlorraine.pierreludmannchessmate.exception;

/**
 * Exception levée lorsqu'une tentative d'enregistrement utilise un email déjà existant.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     * @param message description de la raison du refus d'enregistrement
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}