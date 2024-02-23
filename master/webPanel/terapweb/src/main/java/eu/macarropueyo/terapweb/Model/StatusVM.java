package eu.macarropueyo.terapweb.Model;

/**
 * Valores:
 * 0 - VM no definida
 * 1 - Error al definir VM
 * 2 - VM definida con exito
 * 3 - Error en VM
 * 4 - VM operativa
 */
public enum StatusVM
{
    NOTDEFINED(0),
    ERRORTODEFINE(1),
    ERROR(2),
    READY(3);

    private int status;
    
    private StatusVM(int status)
    {
        this.status=status;
    }

    public int getStatus()
    {
        return this.status;
    }

    public boolean isVoid()
    {
        return this.equals(StatusVM.NOTDEFINED)||this.equals(StatusVM.ERRORTODEFINE);
    }

    public boolean isNotDefinedByError()
    {
        return this.equals(StatusVM.ERRORTODEFINE);
    }

    public boolean isDefined()
    {
        return this.equals(StatusVM.ERROR)||this.equals(StatusVM.READY);
    }

    public boolean isAnError()
    {
        return this.equals(StatusVM.ERROR)||this.equals(StatusVM.ERRORTODEFINE);
    }

    public boolean isOperative()
    {
        return this.equals(StatusVM.READY);
    }
}
