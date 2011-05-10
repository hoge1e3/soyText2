// generated from [[110221_034347]] by [[110222_102732]]
package jp.tonyu.js;
public class PrototypeJS{
  public static final String value=
    "/*  Prototype JavaScript framework, version 1.6.1\n"+
    " *  (c) 2005-2009 Sam Stephenson\n"+
    " *\n"+
    " *  Prototype is freely distributable under the terms of an MIT-style license.\n"+
    " *  For details, see the Prototype web site: http://www.prototypejs.org/\n"+
    " *\n"+
    " *--------------------------------------------------------------------------*/\n"+
    "\n"+
    "var Prototype = {\n"+
    "  Version: '1.6.1',\n"+
    "\n"+
    "  ScriptFragment: '<script[^>]*>([\\\\S\\\\s]*?)<\\/script>',\n"+
    "  JSONFilter: /^\\/\\*-secure-([\\s\\S]*)\\*\\/\\s*$/,\n"+
    "\n"+
    "  emptyFunction: function() { },\n"+
    "  K: function(x) { return x }\n"+
    "};\n"+
    "\n"+
    "var Abstract = { };\n"+
    "\n"+
    "\n"+
    "var Try = {\n"+
    "  these: function() {\n"+
    "    var returnValue;\n"+
    "\n"+
    "    for (var i = 0, length = arguments.length; i < length; i++) {\n"+
    "      var lambda = arguments[i];\n"+
    "      try {\n"+
    "        returnValue = lambda();\n"+
    "        break;\n"+
    "      } catch (e) { }\n"+
    "    }\n"+
    "\n"+
    "    return returnValue;\n"+
    "  }\n"+
    "};\n"+
    "\n"+
    "/* Based on Alex Arnell's inheritance implementation. */\n"+
    "\n"+
    "var Class = (function() {\n"+
    "  function subclass() {};\n"+
    "  function create() {\n"+
    "    var parent = null, properties = $A(arguments);\n"+
    "    if (Object.isFunction(properties[0]))\n"+
    "      parent = properties.shift();\n"+
    "\n"+
    "    function klass() {\n"+
    "      this.initialize.apply(this, arguments);\n"+
    "    }\n"+
    "\n"+
    "    Object.extend(klass, Class.Methods);\n"+
    "    klass.superclass = parent;\n"+
    "    klass.subclasses = [];\n"+
    "\n"+
    "    if (parent) {\n"+
    "      subclass.prototype = parent.prototype;\n"+
    "      klass.prototype = new subclass;\n"+
    "      parent.subclasses.push(klass);\n"+
    "    }\n"+
    "\n"+
    "    for (var i = 0; i < properties.length; i++)\n"+
    "      klass.addMethods(properties[i]);\n"+
    "\n"+
    "    if (!klass.prototype.initialize)\n"+
    "      klass.prototype.initialize = Prototype.emptyFunction;\n"+
    "\n"+
    "    klass.prototype.constructor = klass;\n"+
    "    return klass;\n"+
    "  }\n"+
    "\n"+
    "  function addMethods(source) {\n"+
    "    var ancestor   = this.superclass && this.superclass.prototype;\n"+
    "    var properties = Object.keys(source);\n"+
    "\n"+
    "    if (!Object.keys({ toString: true }).length) {\n"+
    "      if (source.toString != Object.prototype.toString)\n"+
    "        properties.push(\"toString\");\n"+
    "      if (source.valueOf != Object.prototype.valueOf)\n"+
    "        properties.push(\"valueOf\");\n"+
    "    }\n"+
    "\n"+
    "    for (var i = 0, length = properties.length; i < length; i++) {\n"+
    "      var property = properties[i], value = source[property];\n"+
    "      if (ancestor && Object.isFunction(value) &&\n"+
    "          value.argumentNames().first() == \"$super\") {\n"+
    "        var method = value;\n"+
    "        value = (function(m) {\n"+
    "          return function() { return ancestor[m].apply(this, arguments); };\n"+
    "        })(property).wrap(method);\n"+
    "\n"+
    "        value.valueOf = method.valueOf.bind(method);\n"+
    "        value.toString = method.toString.bind(method);\n"+
    "      }\n"+
    "      this.prototype[property] = value;\n"+
    "    }\n"+
    "\n"+
    "    return this;\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    create: create,\n"+
    "    Methods: {\n"+
    "      addMethods: addMethods\n"+
    "    }\n"+
    "  };\n"+
    "})();\n"+
    "(function() {\n"+
    "\n"+
    "  var _toString = Object.prototype.toString;\n"+
    "\n"+
    "  function extend(destination, source) {\n"+
    "    for (var property in source)\n"+
    "      destination[property] = source[property];\n"+
    "    return destination;\n"+
    "  }\n"+
    "\n"+
    "  function inspect(object) {\n"+
    "    try {\n"+
    "      if (isUndefined(object)) return 'undefined';\n"+
    "      if (object === null) return 'null';\n"+
    "      return object.inspect ? object.inspect() : String(object);\n"+
    "    } catch (e) {\n"+
    "      if (e instanceof RangeError) return '...';\n"+
    "      throw e;\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function toJSON(object) {\n"+
    "    var type = typeof object;\n"+
    "    switch (type) {\n"+
    "      case 'undefined':\n"+
    "      case 'function':\n"+
    "      case 'unknown': return;\n"+
    "      case 'boolean': return object.toString();\n"+
    "    }\n"+
    "\n"+
    "    if (object === null) return 'null';\n"+
    "    if (object.toJSON) return object.toJSON();\n"+
    "    if (isElement(object)) return;\n"+
    "\n"+
    "    var results = [];\n"+
    "    for (var property in object) {\n"+
    "      var value = toJSON(object[property]);\n"+
    "      if (!isUndefined(value))\n"+
    "        results.push(property.toJSON() + ': ' + value);\n"+
    "    }\n"+
    "\n"+
    "    return '{' + results.join(', ') + '}';\n"+
    "  }\n"+
    "\n"+
    "  function toQueryString(object) {\n"+
    "    return $H(object).toQueryString();\n"+
    "  }\n"+
    "\n"+
    "  function toHTML(object) {\n"+
    "    return object && object.toHTML ? object.toHTML() : String.interpret(object);\n"+
    "  }\n"+
    "\n"+
    "  function keys(object) {\n"+
    "    var results = [];\n"+
    "    for (var property in object)\n"+
    "      results.push(property);\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function values(object) {\n"+
    "    var results = [];\n"+
    "    for (var property in object)\n"+
    "      results.push(object[property]);\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function clone(object) {\n"+
    "    return extend({ }, object);\n"+
    "  }\n"+
    "\n"+
    "  function isElement(object) {\n"+
    "    return !!(object && object.nodeType == 1);\n"+
    "  }\n"+
    "\n"+
    "  function isArray(object) {\n"+
    "    return _toString.call(object) == \"[object Array]\";\n"+
    "  }\n"+
    "\n"+
    "\n"+
    "  function isHash(object) {\n"+
    "    return object instanceof Hash;\n"+
    "  }\n"+
    "\n"+
    "  function isFunction(object) {\n"+
    "    return typeof object === \"function\";\n"+
    "  }\n"+
    "\n"+
    "  function isString(object) {\n"+
    "    return _toString.call(object) == \"[object String]\";\n"+
    "  }\n"+
    "\n"+
    "  function isNumber(object) {\n"+
    "    return _toString.call(object) == \"[object Number]\";\n"+
    "  }\n"+
    "\n"+
    "  function isUndefined(object) {\n"+
    "    return typeof object === \"undefined\";\n"+
    "  }\n"+
    "\n"+
    "  extend(Object, {\n"+
    "    extend:        extend,\n"+
    "    inspect:       inspect,\n"+
    "    toJSON:        toJSON,\n"+
    "    toQueryString: toQueryString,\n"+
    "    toHTML:        toHTML,\n"+
    "    keys:          keys,\n"+
    "    values:        values,\n"+
    "    clone:         clone,\n"+
    "    isElement:     isElement,\n"+
    "    isArray:       isArray,\n"+
    "    isHash:        isHash,\n"+
    "    isFunction:    isFunction,\n"+
    "    isString:      isString,\n"+
    "    isNumber:      isNumber,\n"+
    "    isUndefined:   isUndefined\n"+
    "  });\n"+
    "})();\n"+
    "Object.extend(Function.prototype, (function() {\n"+
    "  var slice = Array.prototype.slice;\n"+
    "\n"+
    "  function update(array, args) {\n"+
    "    var arrayLength = array.length, length = args.length;\n"+
    "    while (length--) array[arrayLength + length] = args[length];\n"+
    "    return array;\n"+
    "  }\n"+
    "\n"+
    "  function merge(array, args) {\n"+
    "    array = slice.call(array, 0);\n"+
    "    return update(array, args);\n"+
    "  }\n"+
    "\n"+
    "  function argumentNames() {\n"+
    "    var names = this.toString().match(/^[\\s\\(]*function[^(]*\\(([^)]*)\\)/)[1]\n"+
    "      .replace(/\\/\\/.*?[\\r\\n]|\\/\\*(?:.|[\\r\\n])*?\\*\\//g, '')\n"+
    "      .replace(/\\s+/g, '').split(',');\n"+
    "    return names.length == 1 && !names[0] ? [] : names;\n"+
    "  }\n"+
    "\n"+
    "  function bind(context) {\n"+
    "    if (arguments.length < 2 && Object.isUndefined(arguments[0])) return this;\n"+
    "    var __method = this, args = slice.call(arguments, 1);\n"+
    "    return function() {\n"+
    "      var a = merge(args, arguments);\n"+
    "      return __method.apply(context, a);\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function bindAsEventListener(context) {\n"+
    "    var __method = this, args = slice.call(arguments, 1);\n"+
    "    return function(event) {\n"+
    "      var a = update([event || window.event], args);\n"+
    "      return __method.apply(context, a);\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function curry() {\n"+
    "    if (!arguments.length) return this;\n"+
    "    var __method = this, args = slice.call(arguments, 0);\n"+
    "    return function() {\n"+
    "      var a = merge(args, arguments);\n"+
    "      return __method.apply(this, a);\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function delay(timeout) {\n"+
    "    var __method = this, args = slice.call(arguments, 1);\n"+
    "    timeout = timeout * 1000\n"+
    "    return window.setTimeout(function() {\n"+
    "      return __method.apply(__method, args);\n"+
    "    }, timeout);\n"+
    "  }\n"+
    "\n"+
    "  function defer() {\n"+
    "    var args = update([0.01], arguments);\n"+
    "    return this.delay.apply(this, args);\n"+
    "  }\n"+
    "\n"+
    "  function wrap(wrapper) {\n"+
    "    var __method = this;\n"+
    "    return function() {\n"+
    "      var a = update([__method.bind(this)], arguments);\n"+
    "      return wrapper.apply(this, a);\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function methodize() {\n"+
    "    if (this._methodized) return this._methodized;\n"+
    "    var __method = this;\n"+
    "    return this._methodized = function() {\n"+
    "      var a = update([this], arguments);\n"+
    "      return __method.apply(null, a);\n"+
    "    };\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    argumentNames:       argumentNames,\n"+
    "    bind:                bind,\n"+
    "    bindAsEventListener: bindAsEventListener,\n"+
    "    curry:               curry,\n"+
    "    delay:               delay,\n"+
    "    defer:               defer,\n"+
    "    wrap:                wrap,\n"+
    "    methodize:           methodize\n"+
    "  }\n"+
    "})());\n"+
    "\n"+
    "\n"+
    "Date.prototype.toJSON = function() {\n"+
    "  return '\"' + this.getUTCFullYear() + '-' +\n"+
    "    (this.getUTCMonth() + 1).toPaddedString(2) + '-' +\n"+
    "    this.getUTCDate().toPaddedString(2) + 'T' +\n"+
    "    this.getUTCHours().toPaddedString(2) + ':' +\n"+
    "    this.getUTCMinutes().toPaddedString(2) + ':' +\n"+
    "    this.getUTCSeconds().toPaddedString(2) + 'Z\"';\n"+
    "};\n"+
    "\n"+
    "\n"+
    "RegExp.prototype.match = RegExp.prototype.test;\n"+
    "\n"+
    "RegExp.escape = function(str) {\n"+
    "  return String(str).replace(/([.*+?^=!:${}()|[\\]\\/\\\\])/g, '\\\\$1');\n"+
    "};\n"+
    "var PeriodicalExecuter = Class.create({\n"+
    "  initialize: function(callback, frequency) {\n"+
    "    this.callback = callback;\n"+
    "    this.frequency = frequency;\n"+
    "    this.currentlyExecuting = false;\n"+
    "\n"+
    "    this.registerCallback();\n"+
    "  },\n"+
    "\n"+
    "  registerCallback: function() {\n"+
    "    this.timer = setInterval(this.onTimerEvent.bind(this), this.frequency * 1000);\n"+
    "  },\n"+
    "\n"+
    "  execute: function() {\n"+
    "    this.callback(this);\n"+
    "  },\n"+
    "\n"+
    "  stop: function() {\n"+
    "    if (!this.timer) return;\n"+
    "    clearInterval(this.timer);\n"+
    "    this.timer = null;\n"+
    "  },\n"+
    "\n"+
    "  onTimerEvent: function() {\n"+
    "    if (!this.currentlyExecuting) {\n"+
    "      try {\n"+
    "        this.currentlyExecuting = true;\n"+
    "        this.execute();\n"+
    "        this.currentlyExecuting = false;\n"+
    "      } catch(e) {\n"+
    "        this.currentlyExecuting = false;\n"+
    "        throw e;\n"+
    "      }\n"+
    "    }\n"+
    "  }\n"+
    "});\n"+
    "Object.extend(String, {\n"+
    "  interpret: function(value) {\n"+
    "    return value == null ? '' : String(value);\n"+
    "  },\n"+
    "  specialChar: {\n"+
    "    '\\b': '\\\\b',\n"+
    "    '\\t': '\\\\t',\n"+
    "    '\\n': '\\\\n',\n"+
    "    '\\f': '\\\\f',\n"+
    "    '\\r': '\\\\r',\n"+
    "    '\\\\': '\\\\\\\\'\n"+
    "  }\n"+
    "});\n"+
    "\n"+
    "Object.extend(String.prototype, (function() {\n"+
    "\n"+
    "  function prepareReplacement(replacement) {\n"+
    "    if (Object.isFunction(replacement)) return replacement;\n"+
    "    var template = new Template(replacement);\n"+
    "    return function(match) { return template.evaluate(match) };\n"+
    "  }\n"+
    "\n"+
    "  function gsub(pattern, replacement) {\n"+
    "    var result = '', source = this, match;\n"+
    "    replacement = prepareReplacement(replacement);\n"+
    "\n"+
    "    if (Object.isString(pattern))\n"+
    "      pattern = RegExp.escape(pattern);\n"+
    "\n"+
    "    if (!(pattern.length || pattern.source)) {\n"+
    "      replacement = replacement('');\n"+
    "      return replacement + source.split('').join(replacement) + replacement;\n"+
    "    }\n"+
    "\n"+
    "    while (source.length > 0) {\n"+
    "      if (match = source.match(pattern)) {\n"+
    "        result += source.slice(0, match.index);\n"+
    "        result += String.interpret(replacement(match));\n"+
    "        source  = source.slice(match.index + match[0].length);\n"+
    "      } else {\n"+
    "        result += source, source = '';\n"+
    "      }\n"+
    "    }\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function sub(pattern, replacement, count) {\n"+
    "    replacement = prepareReplacement(replacement);\n"+
    "    count = Object.isUndefined(count) ? 1 : count;\n"+
    "\n"+
    "    return this.gsub(pattern, function(match) {\n"+
    "      if (--count < 0) return match[0];\n"+
    "      return replacement(match);\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function scan(pattern, iterator) {\n"+
    "    this.gsub(pattern, iterator);\n"+
    "    return String(this);\n"+
    "  }\n"+
    "\n"+
    "  function truncate(length, truncation) {\n"+
    "    length = length || 30;\n"+
    "    truncation = Object.isUndefined(truncation) ? '...' : truncation;\n"+
    "    return this.length > length ?\n"+
    "      this.slice(0, length - truncation.length) + truncation : String(this);\n"+
    "  }\n"+
    "\n"+
    "  function strip() {\n"+
    "    return this.replace(/^\\s+/, '').replace(/\\s+$/, '');\n"+
    "  }\n"+
    "\n"+
    "  function stripTags() {\n"+
    "    return this.replace(/<\\w+(\\s+(\"[^\"]*\"|'[^']*'|[^>])+)?>|<\\/\\w+>/gi, '');\n"+
    "  }\n"+
    "\n"+
    "  function stripScripts() {\n"+
    "    return this.replace(new RegExp(Prototype.ScriptFragment, 'img'), '');\n"+
    "  }\n"+
    "\n"+
    "  function extractScripts() {\n"+
    "    var matchAll = new RegExp(Prototype.ScriptFragment, 'img');\n"+
    "    var matchOne = new RegExp(Prototype.ScriptFragment, 'im');\n"+
    "    return (this.match(matchAll) || []).map(function(scriptTag) {\n"+
    "      return (scriptTag.match(matchOne) || ['', ''])[1];\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function evalScripts() {\n"+
    "    return this.extractScripts().map(function(script) { return eval(script) });\n"+
    "  }\n"+
    "\n"+
    "  function escapeHTML() {\n"+
    "    return this.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');\n"+
    "  }\n"+
    "\n"+
    "  function unescapeHTML() {\n"+
    "    return this.stripTags().replace(/&lt;/g,'<').replace(/&gt;/g,'>').replace(/&amp;/g,'&');\n"+
    "  }\n"+
    "\n"+
    "\n"+
    "  function toQueryParams(separator) {\n"+
    "    var match = this.strip().match(/([^?#]*)(#.*)?$/);\n"+
    "    if (!match) return { };\n"+
    "\n"+
    "    return match[1].split(separator || '&').inject({ }, function(hash, pair) {\n"+
    "      if ((pair = pair.split('='))[0]) {\n"+
    "        var key = decodeURIComponent(pair.shift());\n"+
    "        var value = pair.length > 1 ? pair.join('=') : pair[0];\n"+
    "        if (value != undefined) value = decodeURIComponent(value);\n"+
    "\n"+
    "        if (key in hash) {\n"+
    "          if (!Object.isArray(hash[key])) hash[key] = [hash[key]];\n"+
    "          hash[key].push(value);\n"+
    "        }\n"+
    "        else hash[key] = value;\n"+
    "      }\n"+
    "      return hash;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function toArray() {\n"+
    "    return this.split('');\n"+
    "  }\n"+
    "\n"+
    "  function succ() {\n"+
    "    return this.slice(0, this.length - 1) +\n"+
    "      String.fromCharCode(this.charCodeAt(this.length - 1) + 1);\n"+
    "  }\n"+
    "\n"+
    "  function times(count) {\n"+
    "    return count < 1 ? '' : new Array(count + 1).join(this);\n"+
    "  }\n"+
    "\n"+
    "  function camelize() {\n"+
    "    var parts = this.split('-'), len = parts.length;\n"+
    "    if (len == 1) return parts[0];\n"+
    "\n"+
    "    var camelized = this.charAt(0) == '-'\n"+
    "      ? parts[0].charAt(0).toUpperCase() + parts[0].substring(1)\n"+
    "      : parts[0];\n"+
    "\n"+
    "    for (var i = 1; i < len; i++)\n"+
    "      camelized += parts[i].charAt(0).toUpperCase() + parts[i].substring(1);\n"+
    "\n"+
    "    return camelized;\n"+
    "  }\n"+
    "\n"+
    "  function capitalize() {\n"+
    "    return this.charAt(0).toUpperCase() + this.substring(1).toLowerCase();\n"+
    "  }\n"+
    "\n"+
    "  function underscore() {\n"+
    "    return this.replace(/::/g, '/')\n"+
    "               .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')\n"+
    "               .replace(/([a-z\\d])([A-Z])/g, '$1_$2')\n"+
    "               .replace(/-/g, '_')\n"+
    "               .toLowerCase();\n"+
    "  }\n"+
    "\n"+
    "  function dasherize() {\n"+
    "    return this.replace(/_/g, '-');\n"+
    "  }\n"+
    "\n"+
    "  function inspect(useDoubleQuotes) {\n"+
    "    var escapedString = this.replace(/[\\x00-\\x1f\\\\]/g, function(character) {\n"+
    "      if (character in String.specialChar) {\n"+
    "        return String.specialChar[character];\n"+
    "      }\n"+
    "      return '\\\\u00' + character.charCodeAt().toPaddedString(2, 16);\n"+
    "    });\n"+
    "    if (useDoubleQuotes) return '\"' + escapedString.replace(/\"/g, '\\\\\"') + '\"';\n"+
    "    return \"'\" + escapedString.replace(/'/g, '\\\\\\'') + \"'\";\n"+
    "  }\n"+
    "\n"+
    "  function toJSON() {\n"+
    "    return this.inspect(true);\n"+
    "  }\n"+
    "\n"+
    "  function unfilterJSON(filter) {\n"+
    "    return this.replace(filter || Prototype.JSONFilter, '$1');\n"+
    "  }\n"+
    "\n"+
    "  function isJSON() {\n"+
    "    var str = this;\n"+
    "    if (str.blank()) return false;\n"+
    "    str = this.replace(/\\\\./g, '@').replace(/\"[^\"\\\\\\n\\r]*\"/g, '');\n"+
    "    return (/^[,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]*$/).test(str);\n"+
    "  }\n"+
    "\n"+
    "  function evalJSON(sanitize) {\n"+
    "    var json = this.unfilterJSON();\n"+
    "    try {\n"+
    "      if (!sanitize || json.isJSON()) return eval('(' + json + ')');\n"+
    "    } catch (e) { }\n"+
    "    throw new SyntaxError('Badly formed JSON string: ' + this.inspect());\n"+
    "  }\n"+
    "\n"+
    "  function include(pattern) {\n"+
    "    return this.indexOf(pattern) > -1;\n"+
    "  }\n"+
    "\n"+
    "  function startsWith(pattern) {\n"+
    "    return this.indexOf(pattern) === 0;\n"+
    "  }\n"+
    "\n"+
    "  function endsWith(pattern) {\n"+
    "    var d = this.length - pattern.length;\n"+
    "    return d >= 0 && this.lastIndexOf(pattern) === d;\n"+
    "  }\n"+
    "\n"+
    "  function empty() {\n"+
    "    return this == '';\n"+
    "  }\n"+
    "\n"+
    "  function blank() {\n"+
    "    return /^\\s*$/.test(this);\n"+
    "  }\n"+
    "\n"+
    "  function interpolate(object, pattern) {\n"+
    "    return new Template(this, pattern).evaluate(object);\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    gsub:           gsub,\n"+
    "    sub:            sub,\n"+
    "    scan:           scan,\n"+
    "    truncate:       truncate,\n"+
    "    strip:          String.prototype.trim ? String.prototype.trim : strip,\n"+
    "    stripTags:      stripTags,\n"+
    "    stripScripts:   stripScripts,\n"+
    "    extractScripts: extractScripts,\n"+
    "    evalScripts:    evalScripts,\n"+
    "    escapeHTML:     escapeHTML,\n"+
    "    unescapeHTML:   unescapeHTML,\n"+
    "    toQueryParams:  toQueryParams,\n"+
    "    parseQuery:     toQueryParams,\n"+
    "    toArray:        toArray,\n"+
    "    succ:           succ,\n"+
    "    times:          times,\n"+
    "    camelize:       camelize,\n"+
    "    capitalize:     capitalize,\n"+
    "    underscore:     underscore,\n"+
    "    dasherize:      dasherize,\n"+
    "    inspect:        inspect,\n"+
    "    toJSON:         toJSON,\n"+
    "    unfilterJSON:   unfilterJSON,\n"+
    "    isJSON:         isJSON,\n"+
    "    evalJSON:       evalJSON,\n"+
    "    include:        include,\n"+
    "    startsWith:     startsWith,\n"+
    "    endsWith:       endsWith,\n"+
    "    empty:          empty,\n"+
    "    blank:          blank,\n"+
    "    interpolate:    interpolate\n"+
    "  };\n"+
    "})());\n"+
    "\n"+
    "var Template = Class.create({\n"+
    "  initialize: function(template, pattern) {\n"+
    "    this.template = template.toString();\n"+
    "    this.pattern = pattern || Template.Pattern;\n"+
    "  },\n"+
    "\n"+
    "  evaluate: function(object) {\n"+
    "    if (object && Object.isFunction(object.toTemplateReplacements))\n"+
    "      object = object.toTemplateReplacements();\n"+
    "\n"+
    "    return this.template.gsub(this.pattern, function(match) {\n"+
    "      if (object == null) return (match[1] + '');\n"+
    "\n"+
    "      var before = match[1] || '';\n"+
    "      if (before == '\\\\') return match[2];\n"+
    "\n"+
    "      var ctx = object, expr = match[3];\n"+
    "      var pattern = /^([^.[]+|\\[((?:.*?[^\\\\])?)\\])(\\.|\\[|$)/;\n"+
    "      match = pattern.exec(expr);\n"+
    "      if (match == null) return before;\n"+
    "\n"+
    "      while (match != null) {\n"+
    "        var comp = match[1].startsWith('[') ? match[2].replace(/\\\\\\\\]/g, ']') : match[1];\n"+
    "        ctx = ctx[comp];\n"+
    "        if (null == ctx || '' == match[3]) break;\n"+
    "        expr = expr.substring('[' == match[3] ? match[1].length : match[0].length);\n"+
    "        match = pattern.exec(expr);\n"+
    "      }\n"+
    "\n"+
    "      return before + String.interpret(ctx);\n"+
    "    });\n"+
    "  }\n"+
    "});\n"+
    "Template.Pattern = /(^|.|\\r|\\n)(#\\{(.*?)\\})/;\n"+
    "\n"+
    "var $break = { };\n"+
    "\n"+
    "var Enumerable = (function() {\n"+
    "  function each(iterator, context) {\n"+
    "    var index = 0;\n"+
    "    try {\n"+
    "      this._each(function(value) {\n"+
    "        iterator.call(context, value, index++);\n"+
    "      });\n"+
    "    } catch (e) {\n"+
    "      if (e != $break) throw e;\n"+
    "    }\n"+
    "    return this;\n"+
    "  }\n"+
    "\n"+
    "  function eachSlice(number, iterator, context) {\n"+
    "    var index = -number, slices = [], array = this.toArray();\n"+
    "    if (number < 1) return array;\n"+
    "    while ((index += number) < array.length)\n"+
    "      slices.push(array.slice(index, index+number));\n"+
    "    return slices.collect(iterator, context);\n"+
    "  }\n"+
    "\n"+
    "  function all(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var result = true;\n"+
    "    this.each(function(value, index) {\n"+
    "      result = result && !!iterator.call(context, value, index);\n"+
    "      if (!result) throw $break;\n"+
    "    });\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function any(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var result = false;\n"+
    "    this.each(function(value, index) {\n"+
    "      if (result = !!iterator.call(context, value, index))\n"+
    "        throw $break;\n"+
    "    });\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function collect(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var results = [];\n"+
    "    this.each(function(value, index) {\n"+
    "      results.push(iterator.call(context, value, index));\n"+
    "    });\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function detect(iterator, context) {\n"+
    "    var result;\n"+
    "    this.each(function(value, index) {\n"+
    "      if (iterator.call(context, value, index)) {\n"+
    "        result = value;\n"+
    "        throw $break;\n"+
    "      }\n"+
    "    });\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function findAll(iterator, context) {\n"+
    "    var results = [];\n"+
    "    this.each(function(value, index) {\n"+
    "      if (iterator.call(context, value, index))\n"+
    "        results.push(value);\n"+
    "    });\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function grep(filter, iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var results = [];\n"+
    "\n"+
    "    if (Object.isString(filter))\n"+
    "      filter = new RegExp(RegExp.escape(filter));\n"+
    "\n"+
    "    this.each(function(value, index) {\n"+
    "      if (filter.match(value))\n"+
    "        results.push(iterator.call(context, value, index));\n"+
    "    });\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function include(object) {\n"+
    "    if (Object.isFunction(this.indexOf))\n"+
    "      if (this.indexOf(object) != -1) return true;\n"+
    "\n"+
    "    var found = false;\n"+
    "    this.each(function(value) {\n"+
    "      if (value == object) {\n"+
    "        found = true;\n"+
    "        throw $break;\n"+
    "      }\n"+
    "    });\n"+
    "    return found;\n"+
    "  }\n"+
    "\n"+
    "  function inGroupsOf(number, fillWith) {\n"+
    "    fillWith = Object.isUndefined(fillWith) ? null : fillWith;\n"+
    "    return this.eachSlice(number, function(slice) {\n"+
    "      while(slice.length < number) slice.push(fillWith);\n"+
    "      return slice;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function inject(memo, iterator, context) {\n"+
    "    this.each(function(value, index) {\n"+
    "      memo = iterator.call(context, memo, value, index);\n"+
    "    });\n"+
    "    return memo;\n"+
    "  }\n"+
    "\n"+
    "  function invoke(method) {\n"+
    "    var args = $A(arguments).slice(1);\n"+
    "    return this.map(function(value) {\n"+
    "      return value[method].apply(value, args);\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function max(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var result;\n"+
    "    this.each(function(value, index) {\n"+
    "      value = iterator.call(context, value, index);\n"+
    "      if (result == null || value >= result)\n"+
    "        result = value;\n"+
    "    });\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function min(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var result;\n"+
    "    this.each(function(value, index) {\n"+
    "      value = iterator.call(context, value, index);\n"+
    "      if (result == null || value < result)\n"+
    "        result = value;\n"+
    "    });\n"+
    "    return result;\n"+
    "  }\n"+
    "\n"+
    "  function partition(iterator, context) {\n"+
    "    iterator = iterator || Prototype.K;\n"+
    "    var trues = [], falses = [];\n"+
    "    this.each(function(value, index) {\n"+
    "      (iterator.call(context, value, index) ?\n"+
    "        trues : falses).push(value);\n"+
    "    });\n"+
    "    return [trues, falses];\n"+
    "  }\n"+
    "\n"+
    "  function pluck(property) {\n"+
    "    var results = [];\n"+
    "    this.each(function(value) {\n"+
    "      results.push(value[property]);\n"+
    "    });\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function reject(iterator, context) {\n"+
    "    var results = [];\n"+
    "    this.each(function(value, index) {\n"+
    "      if (!iterator.call(context, value, index))\n"+
    "        results.push(value);\n"+
    "    });\n"+
    "    return results;\n"+
    "  }\n"+
    "\n"+
    "  function sortBy(iterator, context) {\n"+
    "    return this.map(function(value, index) {\n"+
    "      return {\n"+
    "        value: value,\n"+
    "        criteria: iterator.call(context, value, index)\n"+
    "      };\n"+
    "    }).sort(function(left, right) {\n"+
    "      var a = left.criteria, b = right.criteria;\n"+
    "      return a < b ? -1 : a > b ? 1 : 0;\n"+
    "    }).pluck('value');\n"+
    "  }\n"+
    "\n"+
    "  function toArray() {\n"+
    "    return this.map();\n"+
    "  }\n"+
    "\n"+
    "  function zip() {\n"+
    "    var iterator = Prototype.K, args = $A(arguments);\n"+
    "    if (Object.isFunction(args.last()))\n"+
    "      iterator = args.pop();\n"+
    "\n"+
    "    var collections = [this].concat(args).map($A);\n"+
    "    return this.map(function(value, index) {\n"+
    "      return iterator(collections.pluck(index));\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function size() {\n"+
    "    return this.toArray().length;\n"+
    "  }\n"+
    "\n"+
    "  function inspect() {\n"+
    "    return '#<Enumerable:' + this.toArray().inspect() + '>';\n"+
    "  }\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "\n"+
    "  return {\n"+
    "    each:       each,\n"+
    "    eachSlice:  eachSlice,\n"+
    "    all:        all,\n"+
    "    every:      all,\n"+
    "    any:        any,\n"+
    "    some:       any,\n"+
    "    collect:    collect,\n"+
    "    map:        collect,\n"+
    "    detect:     detect,\n"+
    "    findAll:    findAll,\n"+
    "    select:     findAll,\n"+
    "    filter:     findAll,\n"+
    "    grep:       grep,\n"+
    "    include:    include,\n"+
    "    member:     include,\n"+
    "    inGroupsOf: inGroupsOf,\n"+
    "    inject:     inject,\n"+
    "    invoke:     invoke,\n"+
    "    max:        max,\n"+
    "    min:        min,\n"+
    "    partition:  partition,\n"+
    "    pluck:      pluck,\n"+
    "    reject:     reject,\n"+
    "    sortBy:     sortBy,\n"+
    "    toArray:    toArray,\n"+
    "    entries:    toArray,\n"+
    "    zip:        zip,\n"+
    "    size:       size,\n"+
    "    inspect:    inspect,\n"+
    "    find:       detect\n"+
    "  };\n"+
    "})();\n"+
    "function $A(iterable) {\n"+
    "  if (!iterable) return [];\n"+
    "  if ('toArray' in Object(iterable)) return iterable.toArray();\n"+
    "  var length = iterable.length || 0, results = new Array(length);\n"+
    "  while (length--) results[length] = iterable[length];\n"+
    "  return results;\n"+
    "}\n"+
    "\n"+
    "function $w(string) {\n"+
    "  if (!Object.isString(string)) return [];\n"+
    "  string = string.strip();\n"+
    "  return string ? string.split(/\\s+/) : [];\n"+
    "}\n"+
    "\n"+
    "Array.from = $A;\n"+
    "\n"+
    "\n"+
    "(function() {\n"+
    "  var arrayProto = Array.prototype,\n"+
    "      slice = arrayProto.slice,\n"+
    "      _each = arrayProto.forEach; // use native browser JS 1.6 implementation if available\n"+
    "\n"+
    "  function each(iterator) {\n"+
    "    for (var i = 0, length = this.length; i < length; i++)\n"+
    "      iterator(this[i]);\n"+
    "  }\n"+
    "  if (!_each) _each = each;\n"+
    "\n"+
    "  function clear() {\n"+
    "    this.length = 0;\n"+
    "    return this;\n"+
    "  }\n"+
    "\n"+
    "  function first() {\n"+
    "    return this[0];\n"+
    "  }\n"+
    "\n"+
    "  function last() {\n"+
    "    return this[this.length - 1];\n"+
    "  }\n"+
    "\n"+
    "  function compact() {\n"+
    "    return this.select(function(value) {\n"+
    "      return value != null;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function flatten() {\n"+
    "    return this.inject([], function(array, value) {\n"+
    "      if (Object.isArray(value))\n"+
    "        return array.concat(value.flatten());\n"+
    "      array.push(value);\n"+
    "      return array;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function without() {\n"+
    "    var values = slice.call(arguments, 0);\n"+
    "    return this.select(function(value) {\n"+
    "      return !values.include(value);\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function reverse(inline) {\n"+
    "    return (inline !== false ? this : this.toArray())._reverse();\n"+
    "  }\n"+
    "\n"+
    "  function uniq(sorted) {\n"+
    "    return this.inject([], function(array, value, index) {\n"+
    "      if (0 == index || (sorted ? array.last() != value : !array.include(value)))\n"+
    "        array.push(value);\n"+
    "      return array;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function intersect(array) {\n"+
    "    return this.uniq().findAll(function(item) {\n"+
    "      return array.detect(function(value) { return item === value });\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "\n"+
    "  function clone() {\n"+
    "    return slice.call(this, 0);\n"+
    "  }\n"+
    "\n"+
    "  function size() {\n"+
    "    return this.length;\n"+
    "  }\n"+
    "\n"+
    "  function inspect() {\n"+
    "    return '[' + this.map(Object.inspect).join(', ') + ']';\n"+
    "  }\n"+
    "\n"+
    "  function toJSON() {\n"+
    "    var results = [];\n"+
    "    this.each(function(object) {\n"+
    "      var value = Object.toJSON(object);\n"+
    "      if (!Object.isUndefined(value)) results.push(value);\n"+
    "    });\n"+
    "    return '[' + results.join(', ') + ']';\n"+
    "  }\n"+
    "\n"+
    "  function indexOf(item, i) {\n"+
    "    i || (i = 0);\n"+
    "    var length = this.length;\n"+
    "    if (i < 0) i = length + i;\n"+
    "    for (; i < length; i++)\n"+
    "      if (this[i] === item) return i;\n"+
    "    return -1;\n"+
    "  }\n"+
    "\n"+
    "  function lastIndexOf(item, i) {\n"+
    "    i = isNaN(i) ? this.length : (i < 0 ? this.length + i : i) + 1;\n"+
    "    var n = this.slice(0, i).reverse().indexOf(item);\n"+
    "    return (n < 0) ? n : i - n - 1;\n"+
    "  }\n"+
    "\n"+
    "  function concat() {\n"+
    "    var array = slice.call(this, 0), item;\n"+
    "    for (var i = 0, length = arguments.length; i < length; i++) {\n"+
    "      item = arguments[i];\n"+
    "      if (Object.isArray(item) && !('callee' in item)) {\n"+
    "        for (var j = 0, arrayLength = item.length; j < arrayLength; j++)\n"+
    "          array.push(item[j]);\n"+
    "      } else {\n"+
    "        array.push(item);\n"+
    "      }\n"+
    "    }\n"+
    "    return array;\n"+
    "  }\n"+
    "\n"+
    "  Object.extend(arrayProto, Enumerable);\n"+
    "\n"+
    "  if (!arrayProto._reverse)\n"+
    "    arrayProto._reverse = arrayProto.reverse;\n"+
    "\n"+
    "  Object.extend(arrayProto, {\n"+
    "    _each:     _each,\n"+
    "    clear:     clear,\n"+
    "    first:     first,\n"+
    "    last:      last,\n"+
    "    compact:   compact,\n"+
    "    flatten:   flatten,\n"+
    "    without:   without,\n"+
    "    reverse:   reverse,\n"+
    "    uniq:      uniq,\n"+
    "    intersect: intersect,\n"+
    "    clone:     clone,\n"+
    "    toArray:   clone,\n"+
    "    size:      size,\n"+
    "    inspect:   inspect,\n"+
    "    toJSON:    toJSON\n"+
    "  });\n"+
    "\n"+
    "  var CONCAT_ARGUMENTS_BUGGY = (function() {\n"+
    "    return [].concat(arguments)[0][0] !== 1;\n"+
    "  })(1,2)\n"+
    "\n"+
    "  if (CONCAT_ARGUMENTS_BUGGY) arrayProto.concat = concat;\n"+
    "\n"+
    "  if (!arrayProto.indexOf) arrayProto.indexOf = indexOf;\n"+
    "  if (!arrayProto.lastIndexOf) arrayProto.lastIndexOf = lastIndexOf;\n"+
    "})();\n"+
    "function $H(object) {\n"+
    "  return new Hash(object);\n"+
    "};\n"+
    "\n"+
    "var Hash = Class.create(Enumerable, (function() {\n"+
    "  function initialize(object) {\n"+
    "    this._object = Object.isHash(object) ? object.toObject() : Object.clone(object);\n"+
    "  }\n"+
    "\n"+
    "  function _each(iterator) {\n"+
    "    for (var key in this._object) {\n"+
    "      var value = this._object[key], pair = [key, value];\n"+
    "      pair.key = key;\n"+
    "      pair.value = value;\n"+
    "      iterator(pair);\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function set(key, value) {\n"+
    "    return this._object[key] = value;\n"+
    "  }\n"+
    "\n"+
    "  function get(key) {\n"+
    "    if (this._object[key] !== Object.prototype[key])\n"+
    "      return this._object[key];\n"+
    "  }\n"+
    "\n"+
    "  function unset(key) {\n"+
    "    var value = this._object[key];\n"+
    "    delete this._object[key];\n"+
    "    return value;\n"+
    "  }\n"+
    "\n"+
    "  function toObject() {\n"+
    "    return Object.clone(this._object);\n"+
    "  }\n"+
    "\n"+
    "  function keys() {\n"+
    "    return this.pluck('key');\n"+
    "  }\n"+
    "\n"+
    "  function values() {\n"+
    "    return this.pluck('value');\n"+
    "  }\n"+
    "\n"+
    "  function index(value) {\n"+
    "    var match = this.detect(function(pair) {\n"+
    "      return pair.value === value;\n"+
    "    });\n"+
    "    return match && match.key;\n"+
    "  }\n"+
    "\n"+
    "  function merge(object) {\n"+
    "    return this.clone().update(object);\n"+
    "  }\n"+
    "\n"+
    "  function update(object) {\n"+
    "    return new Hash(object).inject(this, function(result, pair) {\n"+
    "      result.set(pair.key, pair.value);\n"+
    "      return result;\n"+
    "    });\n"+
    "  }\n"+
    "\n"+
    "  function toQueryPair(key, value) {\n"+
    "    if (Object.isUndefined(value)) return key;\n"+
    "    return key + '=' + encodeURIComponent(String.interpret(value));\n"+
    "  }\n"+
    "\n"+
    "  function toQueryString() {\n"+
    "    return this.inject([], function(results, pair) {\n"+
    "      var key = encodeURIComponent(pair.key), values = pair.value;\n"+
    "\n"+
    "      if (values && typeof values == 'object') {\n"+
    "        if (Object.isArray(values))\n"+
    "          return results.concat(values.map(toQueryPair.curry(key)));\n"+
    "      } else results.push(toQueryPair(key, values));\n"+
    "      return results;\n"+
    "    }).join('&');\n"+
    "  }\n"+
    "\n"+
    "  function inspect() {\n"+
    "    return '#<Hash:{' + this.map(function(pair) {\n"+
    "      return pair.map(Object.inspect).join(': ');\n"+
    "    }).join(', ') + '}>';\n"+
    "  }\n"+
    "\n"+
    "  function toJSON() {\n"+
    "    return Object.toJSON(this.toObject());\n"+
    "  }\n"+
    "\n"+
    "  function clone() {\n"+
    "    return new Hash(this);\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    initialize:             initialize,\n"+
    "    _each:                  _each,\n"+
    "    set:                    set,\n"+
    "    get:                    get,\n"+
    "    unset:                  unset,\n"+
    "    toObject:               toObject,\n"+
    "    toTemplateReplacements: toObject,\n"+
    "    keys:                   keys,\n"+
    "    values:                 values,\n"+
    "    index:                  index,\n"+
    "    merge:                  merge,\n"+
    "    update:                 update,\n"+
    "    toQueryString:          toQueryString,\n"+
    "    inspect:                inspect,\n"+
    "    toJSON:                 toJSON,\n"+
    "    clone:                  clone\n"+
    "  };\n"+
    "})());\n"+
    "\n"+
    "Hash.from = $H;\n"+
    "Object.extend(Number.prototype, (function() {\n"+
    "  function toColorPart() {\n"+
    "    return this.toPaddedString(2, 16);\n"+
    "  }\n"+
    "\n"+
    "  function succ() {\n"+
    "    return this + 1;\n"+
    "  }\n"+
    "\n"+
    "  function times(iterator, context) {\n"+
    "    $R(0, this, true).each(iterator, context);\n"+
    "    return this;\n"+
    "  }\n"+
    "\n"+
    "  function toPaddedString(length, radix) {\n"+
    "    var string = this.toString(radix || 10);\n"+
    "    return '0'.times(length - string.length) + string;\n"+
    "  }\n"+
    "\n"+
    "  function toJSON() {\n"+
    "    return isFinite(this) ? this.toString() : 'null';\n"+
    "  }\n"+
    "\n"+
    "  function abs() {\n"+
    "    return Math.abs(this);\n"+
    "  }\n"+
    "\n"+
    "  function round() {\n"+
    "    return Math.round(this);\n"+
    "  }\n"+
    "\n"+
    "  function ceil() {\n"+
    "    return Math.ceil(this);\n"+
    "  }\n"+
    "\n"+
    "  function floor() {\n"+
    "    return Math.floor(this);\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    toColorPart:    toColorPart,\n"+
    "    succ:           succ,\n"+
    "    times:          times,\n"+
    "    toPaddedString: toPaddedString,\n"+
    "    toJSON:         toJSON,\n"+
    "    abs:            abs,\n"+
    "    round:          round,\n"+
    "    ceil:           ceil,\n"+
    "    floor:          floor\n"+
    "  };\n"+
    "})());\n"+
    "\n"+
    "function $R(start, end, exclusive) {\n"+
    "  return new ObjectRange(start, end, exclusive);\n"+
    "}\n"+
    "\n"+
    "var ObjectRange = Class.create(Enumerable, (function() {\n"+
    "  function initialize(start, end, exclusive) {\n"+
    "    this.start = start;\n"+
    "    this.end = end;\n"+
    "    this.exclusive = exclusive;\n"+
    "  }\n"+
    "\n"+
    "  function _each(iterator) {\n"+
    "    var value = this.start;\n"+
    "    while (this.include(value)) {\n"+
    "      iterator(value);\n"+
    "      value = value.succ();\n"+
    "    }\n"+
    "  }\n"+
    "\n"+
    "  function include(value) {\n"+
    "    if (value < this.start)\n"+
    "      return false;\n"+
    "    if (this.exclusive)\n"+
    "      return value < this.end;\n"+
    "    return value <= this.end;\n"+
    "  }\n"+
    "\n"+
    "  return {\n"+
    "    initialize: initialize,\n"+
    "    _each:      _each,\n"+
    "    include:    include\n"+
    "  };\n"+
    "})());";
}