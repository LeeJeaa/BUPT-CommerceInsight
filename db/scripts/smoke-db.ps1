param(
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin"
)

$ErrorActionPreference = "Stop"

docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -c "select version();"
docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -c "select current_database(), current_user, now();"
