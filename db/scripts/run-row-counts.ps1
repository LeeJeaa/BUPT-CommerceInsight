param(
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin"
)

& "$PSScriptRoot/count-tables.ps1" -Container $Container -Database $Database -User $User
