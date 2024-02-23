package eu.macarropueyo.terapweb.Model;


/**
 * Valores:
 * 0 -> todo ok
 * 1 -> atencion - Solo alertas
 * 2 -> fail - fallo importante requiere atencion
 * 3 -> cerrado - no acepta nuevas entradas
 * 4 -> expulse - no acepta nuevas entradas y reubica las actuales
 * 5 -> mantenimiento - no acepta nuevas entradas, las actuales estan paralizadas
 * 6 -> critic - no acepta nuevas entradas, las actuales estan paralizadas, requiere atencion urgente
 * 7 -> dead - no se encuentra el host
 */
public enum StatusHost
{
     OK(0),
     ATENTION(1),
     FAIL(2),
     CLOSE(3),
     EXPULSE(4),
     MAINTENANCE(5),
     CRITIC(6),
     DEAD(7);

    private int status;

    private StatusHost(int status)
    {
        this.status=status;
    }

    public int getStatus()
    {
        return status;
    }

    public boolean isGood()
    {
        return this.equals(StatusHost.OK);
    }

    public boolean isAvailable()
    {
        return !this.equals(StatusHost.DEAD);
    }

    public boolean acceptEntries()
    {
        return this.equals(StatusHost.OK)||this.equals(StatusHost.ATENTION)||this.equals(StatusHost.FAIL);
    }
}
