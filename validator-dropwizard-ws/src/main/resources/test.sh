#!/usr/bin/env bash
curl -i -F "file=@/Users/fmendez/dev/git/gbif/gbif-data-validator/validator-core/src/test/resources/0008759-160822134323880.csvar" http://localhost:8080/validate/file
