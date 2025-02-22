#!/usr/bin/env sh

VER=$(git tag --sort=committerdate | grep -E '^1\.' | tail -n1)

echo "Current version is $VER, what is the next version?"
read -r NEW_VER

sed -i '' "s/$VER/$NEW_VER/g" version.sbt
sed -i '' "s/$VER/$NEW_VER/g" README.md
sed -i '' "s/scala.uri.ver=$VER/scala.uri.ver=$NEW_VER/g" .travis.yml

git commit -am"Bump version to $NEW_VER"
git tag $NEW_VER
