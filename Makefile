##
# Streamline Language Makefile
# @file
# @version 0.1

.PHONY: protogen
protogen:
	protoc --clojure_out=src/ proto/*
# end
