package eu.macarropueyo.terapweb.Model;

public enum StatusUser
{
    OK(0),
    BLOCK(-1),
    NOTEXIST(1);

    private int status;

    private StatusUser(int status)
    {
        this.status=status;
    }

    public int getStatus()
    {
        return this.status;
    }

    public boolean isBlock()
    {
        return this.equals(StatusUser.BLOCK);
    }

    /**
     * Indica si el usuario pertenece al dominio (LDAP) o si solo existe en la base de datos.
     * EXPERIMENTAL.
     * @return
     */
    public boolean notDomain()
    {
        return getStatus()>0;
    }
}
