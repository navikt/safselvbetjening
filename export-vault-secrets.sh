#!/usr/bin/env sh
if test -f /secrets/serviceuser/srvsafselvbetjening/username;
then
    echo "Setting safselvbetjening_serviceuser_username"
    export safselvbetjening_serviceuser_username=$(cat /secrets/serviceuser/srvsafselvbetjening/username)
fi
if test -f /secrets/serviceuser/srvsafselvbetjening/password;
then
    echo "Setting safselvbetjening_serviceuser_password"
    export safselvbetjening_serviceuser_password=$(cat /secrets/serviceuser/srvsafselvbetjening/password)
fi
